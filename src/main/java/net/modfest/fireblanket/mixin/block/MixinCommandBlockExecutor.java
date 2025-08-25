package net.modfest.fireblanket.mixin.block;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.world.CommandBlockExecutor;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * @author Ampflower
 **/
@Mixin(CommandBlockExecutor.class)
public class MixinCommandBlockExecutor implements CommandBE {
	@Unique
	private UUID fireblanket$owner;
	@Unique
	private UUID fireblanket$lastUpdated;

	/**
	 * @return self as {@link CommandBlockExecutor}
	 */
	@Override
	public CommandBlockExecutor fireblanket$getCommandExecutor() {
		//noinspection ConstantConditions
		return (CommandBlockExecutor) (Object) this;
	}

	@Inject(method = "writeData", at = @At("TAIL"))
	private void fireblanket$writeNbt(WriteView nbt, CallbackInfo ci) {
		if (fireblanket$owner != null) {
			nbt.put("FB:Owner", Uuids.INT_STREAM_CODEC, fireblanket$owner);
		}

		if (fireblanket$lastUpdated != null) {
			nbt.put("FB:LastUpdated", Uuids.INT_STREAM_CODEC, fireblanket$lastUpdated);
		}
	}

	@Inject(method = "readData", at = @At("TAIL"))
	private void fireblanket$readNbt(ReadView nbt, CallbackInfo ci) {
		fireblanket$owner = nbt.read("FB:Owner", Uuids.INT_STREAM_CODEC).orElse(null);
		fireblanket$lastUpdated = nbt.read("FB:LastUpdated", Uuids.INT_STREAM_CODEC).orElse(null);
	}

	@Override
	public void fireblanket$setOwner(final UUID uuid) {
		this.fireblanket$owner = uuid;
	}

	@Override
	public void fireblanket$setLastUpdate(final UUID uuid) {
		this.fireblanket$lastUpdated = uuid;
	}

	@Override
	public UUID fireblanket$getOwner() {
		return this.fireblanket$owner;
	}

	@Override
	public UUID fireblanket$getLastUpdate() {
		return this.fireblanket$lastUpdated;
	}
}
