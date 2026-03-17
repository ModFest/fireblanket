package net.modfest.fireblanket.mixin.mods.masking.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask")
public class MixinChunkRenderRebuildTask {
//	@Inject(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;get(Lnet/minecraft/block/entity/BlockEntity;)Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
//	private void fireblanket$AddBEAnyway_Sodium(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, BuiltSectionInfo.Builder renderData, ChunkOcclusionDataBuilder occluder, ChunkBuildBuffers buffers, BlockRenderCache cache, WorldSlice slice, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockPos.Mutable blockPos, BlockPos.Mutable modelOffset, BlockRenderContext context, int y, int z, int x, BlockState blockState, FluidState fluidState, BlockEntity entity, BlockEntityRenderer renderer) {
//		if (renderer == null && ClientState.MASKED_BERS.contains(entity.getType())) {
//			renderData.addBlockEntity(entity, false);
//		}
//	}
}
