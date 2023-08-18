package net.modfest.fireblanket.client;

import net.minecraft.entity.LivingEntity;

public class EntityTick {
    public static void minimalTick(LivingEntity e) {
        e.prevPitch = e.getPitch();
        e.prevYaw = e.getYaw();
        // TODO: seems like only players use this?
//        e.prevHorizontalSpeed = e.horizontalSpeed;
    }
}
