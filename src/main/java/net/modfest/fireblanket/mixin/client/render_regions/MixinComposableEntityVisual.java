package net.modfest.fireblanket.mixin.client.render_regions;

//import dev.engine_room.flywheel.api.visualization.VisualizationContext;
//import dev.engine_room.vanillin.compose.ConfiguredElement;
//import dev.engine_room.vanillin.compose.VisualizationPredicate;
import net.minecraft.entity.Entity;
import net.modfest.fireblanket.FireblanketClient;
import net.modfest.fireblanket.client.render.RenderRegionRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
