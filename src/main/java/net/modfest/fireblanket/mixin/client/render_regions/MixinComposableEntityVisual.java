package net.modfest.fireblanket.mixin.client.render_regions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "dev.engine_room.vanillin.compose.ComposableEntityVisual")
public class MixinComposableEntityVisual {
//	@Redirect(method = "updateElements", at = @At(value = "INVOKE", target = "Ldev/engine_room/vanillin/compose/VisualizationPredicate;shouldVisualize(Ldev/engine_room/flywheel/api/visualization/VisualizationContext;Ljava/lang/Object;)Z", remap = false), remap = false)
//	private boolean fireblanket$renderRegion(VisualizationPredicate instance, VisualizationContext visualizationContext, Object t) {
//		return
////		false
//			instance.shouldVisualize(visualizationContext, t)
//			&& (FireblanketClient.shouldRender((Entity) t)
//			|| !RenderRegionRenderer.useRegionRenderer)
//			;
//	}
//
//	@Redirect(method = "updateElements", at = @At(value = "INVOKE", target = "Ldev/engine_room/vanillin/compose/ConfiguredElement;shouldVisualize(Ldev/engine_room/flywheel/api/visualization/VisualizationContext;Ljava/lang/Object;)Z", remap = false), remap = false)
//	private boolean fireblanket$renderRegionEle(ConfiguredElement instance, VisualizationContext visualizationContext, Object t) {
//		return
//			instance.shouldVisualize(visualizationContext, t)
//			&& (FireblanketClient.shouldRender((Entity) t)
//			|| !RenderRegionRenderer.useRegionRenderer)
//			;
//	}
}
