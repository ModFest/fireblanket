package net.modfest.fireblanket.mixin.client;

import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.util.math.MathHelper;
import net.modfest.fireblanket.Fireblanket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(SpriteLoader.class)
public class MixinSpriteLoader {
	@Inject(method = "stitch", at = @At("HEAD"))
	private void fireblanket$allMipLevelLowers(List<SpriteContents> sprites, int mipLevel, Executor executor, CallbackInfoReturnable<SpriteLoader.StitchResult> cir) {
		int k = 1 << mipLevel;

		for (SpriteContents spriteContents : sprites) {
			int l = Math.min(Integer.lowestOneBit(spriteContents.getWidth()), Integer.lowestOneBit(spriteContents.getHeight()));
			if (l < k) {
				Fireblanket.LOGGER.warn(
					"(Fireblanket-AllStitchErrors) Texture {} with size {}x{} limits mip level from {} to {}",
					spriteContents.getId(),
					spriteContents.getWidth(),
					spriteContents.getHeight(),
					MathHelper.floorLog2(k),
					MathHelper.floorLog2(l)
				);
			}
		}
	}
}
