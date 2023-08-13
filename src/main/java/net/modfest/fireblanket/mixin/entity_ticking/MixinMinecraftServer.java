package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.modfest.fireblanket.world.entity.EntityFilters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(method = "createWorlds", at = @At("HEAD"))
    private void fireblanket$setupEntityTypeFilters(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        EntityFilters.apply();
    }
}
