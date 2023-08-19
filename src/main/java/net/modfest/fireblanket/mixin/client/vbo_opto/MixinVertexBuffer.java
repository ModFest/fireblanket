package net.modfest.fireblanket.mixin.client.vbo_opto;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VertexBuffer.class)
public class MixinVertexBuffer {
	@Redirect(method = "drawInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/ShaderProgram;addSampler(Ljava/lang/String;Ljava/lang/Object;)V"))
	private void fireblanket$optimizeVBODraw(ShaderProgram instance, String name, Object sampler) {
		if (((Integer)sampler) != 0) {
			instance.addSampler(name, sampler);
		}
	}
}
