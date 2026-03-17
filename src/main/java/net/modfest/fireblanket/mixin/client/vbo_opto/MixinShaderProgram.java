package net.modfest.fireblanket.mixin.client.vbo_opto;

import com.mojang.blaze3d.opengl.GlProgram;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GlProgram.class)
public class MixinShaderProgram {
//	@Mutable
//	@Shadow
//	@Final
//	private Map<String, Object> samplers;
//
//	@Inject(method = "<init>", at = @At("TAIL"))
//	private void fireblanket$injectBetterMap(ResourceFactory factory, String name, VertexFormat format, CallbackInfo ci) {
//		this.samplers = new Object2ObjectOpenHashMap<>(4);
//	}
//
//	// TODO: Why does this exist?
//	@Redirect(method = "initializeUniforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/ShaderProgram;addSampler(Ljava/lang/String;Ljava/lang/Object;)V"))
//	private void fireblanket$optimizeVBODraw(ShaderProgram instance, String name, Object sampler) {
//		if (((Integer) sampler) != 0) {
//			instance.addSampler(name, sampler);
//		}
//	}
}
