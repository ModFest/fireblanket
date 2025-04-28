package net.modfest.fireblanket.mixin.client;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	/**
	 * @author jaskarth
	 *
	 * @reason sorry
	 */
	@Overwrite
	private void checkEmpty(MatrixStack matrices) {
		while (!matrices.isEmpty()) {
			matrices.pop();
		}
	}
}
