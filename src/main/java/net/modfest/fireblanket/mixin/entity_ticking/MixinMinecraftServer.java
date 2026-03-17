package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.modfest.fireblanket.world.WorldLoadAppliers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
	@Inject(method = "createLevels", at = @At("HEAD"))
	private void fireblanket$setupEntityTypeFilters(ChunkProgressListener worldGenerationProgressListener, CallbackInfo ci) {
		WorldLoadAppliers.init();
	}
}
