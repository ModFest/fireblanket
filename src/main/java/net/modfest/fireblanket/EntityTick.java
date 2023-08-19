package net.modfest.fireblanket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class EntityTick {
	public static void minimalTick(Entity e) {
		e.prevPitch = e.getPitch();
		e.prevYaw = e.getYaw();
		e.prevHorizontalSpeed = e.horizontalSpeed;
		e.prevX = e.lastRenderX = e.getX();
		e.prevY = e.lastRenderY = e.getY();
		e.prevZ = e.lastRenderZ = e.getZ();
	}
	public static void minimalTick(LivingEntity e) {
		minimalTick((Entity)e);
		e.prevHeadYaw = e.headYaw;
		e.prevBodyYaw = e.bodyYaw;
		e.lastHandSwingProgress = e.handSwingProgress;
	}
}
