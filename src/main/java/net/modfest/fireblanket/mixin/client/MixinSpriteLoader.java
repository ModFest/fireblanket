package net.modfest.fireblanket.mixin.client;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.util.Mth;
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
	private void fireblanket$allMipLevelLowers(List<SpriteContents> sprites, int mipLevel, Executor executor, CallbackInfoReturnable<SpriteLoader.Preparations> cir) {
		int k = 1 << mipLevel;

		for (SpriteContents spriteContents : sprites) {
			int l = Math.min(Integer.lowestOneBit(spriteContents.width()), Integer.lowestOneBit(spriteContents.height()));
			if (l < k) {
				Fireblanket.LOGGER.warn(
					"(Fireblanket-AllStitchErrors) Texture {} with size {}x{} limits mip level from {} to {}",
					spriteContents.name(),
					spriteContents.width(),
					spriteContents.height(),
					Mth.log2(k),
					Mth.log2(l)
				);
			}
		}
	}
}
