package net.modfest.fireblanket.mixin.block_format;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.modfest.fireblanket.world.blocks.FlatBlockstateArray;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Augments the chunk palette to primarily use a flat array to store blockstate ids, speeding up blockstate access and
 * setting. As many minecraft structures rely on the palette, when the palette is queried, for saving or networking,
 * it flushes the data from the flat array back into the palette.
 */
@Mixin(ChunkSection.class)
public abstract class MixinChunkSection {
	// Will be real due to mixin plugin

	private static final int MASK_BITS = 1048575;

	@Shadow @Final private PalettedContainer<BlockState> blockStateContainer;
	@Shadow private short nonEmptyBlockCount;
	@Shadow private short nonEmptyFluidCount;
	@Shadow private short randomTickableBlockCount;

	// 20 bits per block, so 3 blocks per long. ceil(4096/3) --> 1366
	private final long[] fireblanket$denseBlockStorage = new long[1366];
	private final BitSet fireblanket$dirty = new BitSet(4096);

	private final AtomicLong fireblanket$stamp = new AtomicLong();

	@Redirect(method = "<init>(Lnet/minecraft/world/chunk/PalettedContainer;Lnet/minecraft/world/chunk/ReadableContainer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;calculateCounts()V"))
	private void fireblanket$setupState(ChunkSection instance) {
		PalettedContainer<BlockState> container = this.blockStateContainer;

		fireblanket$applyFromPalette(container);
	}

	private void fireblanket$applyFromPalette(PalettedContainer<BlockState> container) {
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					setBlockState(x, y, z, container.get(x, y, z), false);
				}
			}
		}

		fireblanket$dirty.clear();
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Optimized with flat array
	 */
	@Overwrite
	public BlockState setBlockState(int x, int y, int z, BlockState state, boolean lock) {
		// Calculate math needed for both stages before the barrier
		int rawIdx = arrayIndex(x, y, z);
		int arrIdx = rawIdx / 3;
		int shlIdx = rawIdx % 3;
		int shift = 20 * shlIdx;

		// Grab a stamp to compare against after we're done writing, just in case anything weird happens
		long stamp = this.fireblanket$stamp.incrementAndGet();

		BlockState oldState;
		try {
			// Get the old state that we already see
			long oldBits = this.fireblanket$denseBlockStorage[arrIdx];
			oldState = FlatBlockstateArray.FROM_ID[((int)(oldBits >>> shift) & MASK_BITS)];

			// Make data for the new state
			long newId = Block.STATE_IDS.getRawId(state);
			long newBitsIn = newId << shift;
			long mask = (long) MASK_BITS << shift;

			// Replace old location with zeros, then apply the new bits
			long newBits = (oldBits & ~mask) | (newBitsIn & mask);

			// Commit to memory, mark dirty
			this.fireblanket$denseBlockStorage[arrIdx] = newBits;
			fireblanket$dirty.set(rawIdx);
		} finally {
			// Nightmare scenario. The stamp is not the same as the one we obtained at the start, so
			// we were probably written to concurrently. This is bad, we need to stop immediately.
			if (this.fireblanket$stamp.get() != stamp) {
				Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
				String dumps = traces.entrySet().stream()
						.map(MixinChunkSection::formatThreadDump)
						.collect(Collectors.joining("\n"));

				String error = "Accessing ChunkSection from multiple threads!";
				CrashReport crashReport = new CrashReport(error, new IllegalStateException(error));
				CrashReportSection crashReportSection = crashReport.addElement("Thread dumps");
				crashReportSection.add("Thread dumps", dumps);
				throw new CrashException(crashReport);
			}
		}

		// Vanilla counting logic

		FluidState fluidState = oldState.getFluidState();
		FluidState fluidState2 = state.getFluidState();
		if (!oldState.isAir()) {
			--this.nonEmptyBlockCount;
			if (oldState.hasRandomTicks()) {
				--this.randomTickableBlockCount;
			}
		}

		if (!fluidState.isEmpty()) {
			--this.nonEmptyFluidCount;
		}

		if (!state.isAir()) {
			++this.nonEmptyBlockCount;
			if (state.hasRandomTicks()) {
				++this.randomTickableBlockCount;
			}
		}

		if (!fluidState2.isEmpty()) {
			++this.nonEmptyFluidCount;
		}

		return oldState;
	}

	private static String formatThreadDump(Map.Entry<Thread, StackTraceElement[]> e) {
		return e.getKey().getName() + ": \n\tat " + Arrays.stream(e.getValue()).map(Object::toString).collect(Collectors.joining("\n\tat "));
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Optimized with flat array
	 */
	@Overwrite
	public BlockState getBlockState(int x, int y, int z) {
		int rawIdx = arrayIndex(x, y, z);

		// Optimized divmod routine
		int divRes = (rawIdx * 0xAAAB) >>> 17;             // rawIdx / 3;
		int modRes = -((divRes + (divRes << 1)) - rawIdx);  // rawIdx % 3;

		long rawVal = (this.fireblanket$denseBlockStorage[divRes] >>> (20L * modRes)) & MASK_BITS;

		return FlatBlockstateArray.FROM_ID[((int) rawVal)];
	}

	private static int arrayIndex(int x, int y, int z) {
		// Y before XZ is typically the access pattern found in extended periods of blockstate lookup
		return y * 256 + x * 16 + z;
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Optimized with flat array. Vanilla duplicates the getBlockState logic, whereas here just calls the
	 * method for simplicity
	 */
	@Overwrite
	public FluidState getFluidState(int x, int y, int z) {
		return getBlockState(x, y, z).getFluidState();
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Flush all updates to the container
	 */
	@Overwrite
	public PalettedContainer<BlockState> getBlockStateContainer() {
		if (fireblanket$dirty.cardinality() > 0) {
			for (int i = 0; i < 4096; i++) {
				if (fireblanket$dirty.get(i)) {
					int y = (i >> 8) & 15;
					int x = (i >> 4) & 15;
					int z = (i >> 0) & 15;

					this.blockStateContainer.swapUnsafe(x, y, z, getBlockState(x, y, z));
				}
			}
		}

		fireblanket$dirty.clear();

		return this.blockStateContainer;
	}

	// Originally this was one injector, but broke horribly, so we're doing it the spacious way instead
	@Inject(method = "toPacket", at = @At("HEAD"))
	private void fireblanket$resetUnderlyingStateToPacket(PacketByteBuf buf, CallbackInfo ci) {
		getBlockStateContainer();
	}

	@Inject(method = "getPacketSize", at = @At("HEAD"))
	private void fireblanket$resetUnderlyingStateGetPacketSize(CallbackInfoReturnable<Integer> cir) {
		getBlockStateContainer();
	}

	@Inject(method = "hasAny", at = @At("HEAD"))
	private void fireblanket$resetUnderlyingStateHasAny(Predicate<BlockState> predicate, CallbackInfoReturnable<Boolean> cir) {
		getBlockStateContainer();
	}

	// Dear god please no one use this on their client. Support is provided for completeness.
	@Inject(method = "readDataPacket", at = @At("TAIL"))
	private void fireblanket$resetForPacketBadTerrible(PacketByteBuf buf, CallbackInfo ci) {
		fireblanket$applyFromPalette(this.blockStateContainer);
	}

	/**
	 * @author Jasmine
	 *
	 * @reason We don't really need this
	 */
	@Overwrite
	public void calculateCounts() {
		// Only ever used in one place, which is redirected, so I figure it's better to implement this on a need basis.
		throw new UnsupportedOperationException("Not implemented");
	}
}
