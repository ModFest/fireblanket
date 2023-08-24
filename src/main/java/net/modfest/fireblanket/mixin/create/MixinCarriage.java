package net.modfest.fireblanket.mixin.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.trains.entity.Carriage", remap = false)
public class MixinCarriage {
    /**
     * @author IThundxr
     * @reason This is not needed for the con since this is mostly a survival feature.
     */
    @Overwrite
    public boolean isOnIncompatibleTrack() {
        return false;
    }
}
