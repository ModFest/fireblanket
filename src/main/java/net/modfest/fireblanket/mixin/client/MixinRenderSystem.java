package net.modfest.fireblanket.mixin.client;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.mojang.blaze3d.systems.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;

import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.client.BlamefulRenderCall;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {

	@Shadow @Final
	private static ConcurrentLinkedQueue<RenderCall> recordingQueue;
	
	/**
	 * @author unascribed
	 * @reason Add blame and log when dubious things occur
	 */
	@Overwrite
	public static void recordRenderCall(RenderCall renderCall) {
		if (!RenderSystem.isOnRenderThread()) {
			Fireblanket.LOGGER.warn("A render call was made off the render thread! This is likely to lead to race conditions!", new Throwable());
		}
		recordingQueue.add(new BlamefulRenderCall(renderCall));
	}
	
}
