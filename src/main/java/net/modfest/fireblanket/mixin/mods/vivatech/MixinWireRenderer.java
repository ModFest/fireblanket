package net.modfest.fireblanket.mixin.mods.vivatech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "falseresync.vivatech.client.rendering.world.WireRenderer")
public class MixinWireRenderer {
	/**
	 * @author jaskarth
	 *
	 * @reason Won't disappear from world, keeps rendering
	 */
//	@Overwrite(remap = false)
//	public void afterEntities(WorldRenderContext context) {
//	}
}
