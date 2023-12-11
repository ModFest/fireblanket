package net.modfest.fireblanket.mixin.client.vbo_opto;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ShaderProgram.class)
public class MixinShaderProgram {
	@Mutable
	@Shadow @Final private Map<String, Object> samplers;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fireblanket$injectBetterMap(ResourceFactory factory, String name, VertexFormat format, CallbackInfo ci) {
		this.samplers = new Object2ObjectOpenHashMap<>(4);
	}
}
