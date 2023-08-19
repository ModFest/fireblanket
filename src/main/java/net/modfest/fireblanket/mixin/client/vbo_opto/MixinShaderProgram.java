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
	private final Object2IntOpenHashMap<String> fireblanket$uniformCache = fireblanket$createMap();

	@Redirect(method = "bind", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;getUniformLocation(ILjava/lang/CharSequence;)I"))
	private int fireblanket$getUniformLocationCache(int program, CharSequence name) {
		int cached = fireblanket$uniformCache.getInt(name);

		if (cached == -1) {
			cached = GlUniform.getUniformLocation(program, name);
			fireblanket$uniformCache.put((String) name, cached);
		}

		return cached;
	}

	private static Object2IntOpenHashMap<String> fireblanket$createMap() {
		Object2IntOpenHashMap<String> m = new Object2IntOpenHashMap<>();
		m.defaultReturnValue(-1);
		return m;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fireblanket$injectBetterMap(ResourceFactory factory, String name, VertexFormat format, CallbackInfo ci) {
		this.samplers = new Object2ObjectOpenHashMap<>(4);
	}
}
