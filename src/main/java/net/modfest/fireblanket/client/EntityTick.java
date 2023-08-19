package net.modfest.fireblanket.client;

import net.minecraft.entity.LivingEntity;

public class EntityTick {
    public static void minimalTick(LivingEntity e) {
        e.prevPitch = e.getPitch();
        e.prevYaw = e.getYaw();
        e.prevHeadYaw = e.headYaw;
        e.prevBodyYaw = e.bodyYaw;

        // TODO: seems like only players use this?
//        e.prevHorizontalSpeed = e.horizontalSpeed;
    }
}
