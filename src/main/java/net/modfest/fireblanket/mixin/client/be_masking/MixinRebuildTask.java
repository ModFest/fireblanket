package net.modfest.fireblanket.mixin.client.be_masking;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SectionRenderDispatcher.RenderSection.RebuildTask.class)
public class MixinRebuildTask {
	// todo
//	@Inject(method = "addBlockEntity", at = @At("TAIL"))
//	private <E extends BlockEntity> void fireblanket$addBEAnyway(ChunkBuilder.BuiltChunk.RebuildTask.RenderData renderData, E blockEntity, CallbackInfo ci) {
//		if (!renderData.blockEntities.contains(blockEntity) && ClientState.MASKED_BERS.contains(blockEntity.getType())) {
//			renderData.blockEntities.add(blockEntity);
//		}
//	}
}
