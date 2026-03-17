package net.modfest.fireblanket.mixin.be_sync;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.modfest.fireblanket.net.BEUpdate;
import net.modfest.fireblanket.net.BatchedBEUpdatePayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder extends GenerationChunkHolder {
	private static final List<BEUpdate> BATCHED_UPDATES = new ArrayList<>();

	public MixinChunkHolder(ChunkPos pos) {
		super(pos);
	}

	@Shadow
	protected abstract void broadcastBlockEntity(List<ServerPlayer> players, Level world, BlockPos pos);

	@Shadow
	protected abstract void broadcast(List<ServerPlayer> players, Packet<?> packet);

	@Shadow @Final
	private ChunkHolder.PlayerProvider playerProvider;

	/**
	 * @author Jasmine
	 *
	 * @reason Don't send BE update packets until the observable state of the BE has *actually* changed.
	 */
//	@Overwrite
//	private void tryUpdateBlockEntityAt(List<ServerPlayerEntity> players, World world, BlockPos pos, BlockState state) {
//		if (state.hasBlockEntity()) {
//			// TODO: this code here is duplicated because mods mixin to sendBlockEntityUpdatePacket, which makes it less ideal for mixing into
//			BlockEntity blockEntity = world.getBlockEntity(pos);
//
//			if (blockEntity != null) {
//				CachedCompoundBE cbe = (CachedCompoundBE) blockEntity;
//				Packet<?> packet = blockEntity.toUpdatePacket();
//				if (packet instanceof BlockEntityUpdateS2CPacket bes2c) {
//					NbtCompound cached = cbe.fireblanket$getCachedCompound();
//					NbtCompound nbt = bes2c.getNbt();
//
//					// Don't update if we're the same as before.
//
//					// TODO: do this over the network thread, so it doesn't block the main thread
//					if (Objects.equals(cached, nbt)) {
//						return;
//					}
//
//					cbe.fireblanket$setCachedCompound(nbt);
//				}
//
//				this.sendBlockEntityUpdatePacket(players, world, pos);
//			}
//		}
//	}
	@Redirect(method = "broadcastBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;broadcast(Ljava/util/List;Lnet/minecraft/network/protocol/Packet;)V"))
	private void fireblanket$dontSendUpdate(ChunkHolder instance, List<ServerPlayer> players, Packet<?> packet) {
		if (packet instanceof ClientboundBlockEntityDataPacket bes2c) {
			// We're a chunk update- let's batch per chunk
			BATCHED_UPDATES.add(new BEUpdate(bes2c.getPos(), bes2c.getType(), bes2c.getTag()));
		} else {
			// No idea what we are- need to fall back to worst case
			this.broadcast(players, packet);
		}
	}

	@Inject(method = "broadcastChanges", at = @At("HEAD"))
	private void fireblanket$flushUpdates$head(LevelChunk chunk, CallbackInfo ci) {
		// Should probably assert here for the list being clear

		BATCHED_UPDATES.clear();
	}

	@Inject(method = "broadcastChanges", at = @At("TAIL"))
	private void fireblanket$flushUpdates$tail(LevelChunk chunk, CallbackInfo ci) {
		if (!BATCHED_UPDATES.isEmpty()) {
			List<ServerPlayer> list = this.playerProvider.getPlayers(this.pos, false);

			int size = BATCHED_UPDATES.size();
			if (list.isEmpty()) {
				BATCHED_UPDATES.clear();
				return;
			}

			for (ServerPlayer p : list) {
				ServerPlayNetworking.send(p, new BatchedBEUpdatePayload(new ArrayList<>(BATCHED_UPDATES)));
			}

			BATCHED_UPDATES.clear();
		}
	}
}
