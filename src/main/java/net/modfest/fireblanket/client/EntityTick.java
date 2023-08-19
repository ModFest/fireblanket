package net.modfest.fireblanket.client;

import net.minecraft.entity.LivingEntity;

public class EntityTick {
	public static void minimalTick(LivingEntity e) {
		e.prevPitch = e.getPitch();
		e.prevYaw = e.getYaw();
		e.prevHeadYaw = e.headYaw;
		e.prevBodyYaw = e.bodyYaw;
		e.prevHorizontalSpeed = e.horizontalSpeed;
		e.prevX = e.lastRenderX = e.getX();
		e.prevY = e.lastRenderY = e.getY();
		e.prevZ = e.lastRenderZ = e.getZ();
		e.lastHandSwingProgress = e.handSwingProgress;
	}
}
