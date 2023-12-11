package net.modfest.fireblanket.mixin.client.be_masking.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.modfest.fireblanket.client.ClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask")
public class MixinChunkRenderRebuildTask {
	@Inject(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;get(Lnet/minecraft/block/entity/BlockEntity;)Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;", shift = At.Shift.AFTER), locals = LocalCapture.PRINT)
	private void fireblanket$AddBEAnyway_Sodium(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, BuiltSectionInfo.Builder renderData, ChunkOcclusionDataBuilder occluder, ChunkBuildBuffers buffers, BlockRenderCache cache, WorldSlice slice, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockPos.Mutable blockPos, BlockPos.Mutable modelOffset, BlockRenderContext context, int y, int z, int x, BlockState blockState, FluidState fluidState, BlockEntity entity, BlockEntityRenderer renderer) {
		if (renderer == null && ClientState.MASKED_BERS.contains(entity.getType())) {
			renderData.addBlockEntity(entity, false);
		}
	}
}
