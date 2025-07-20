package net.modfest.fireblanket.world.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class EntityTick {
	public static void minimalTick(Entity e) {
		e.resetPosition();
	}

//	public static void minimalLivingTick(LivingEntity e) {
//		minimalTick(e);
//		e.lastHeadYaw = e.headYaw;
//		e.lastBodyYaw = e.bodyYaw;
//		e.lastHandSwingProgress = e.handSwingProgress;
//
//		if (e.bodyTrackingIncrements > 0) {
//			double x = e.getX() + (e.serverX - e.getX()) / e.bodyTrackingIncrements;
//			double y = e.getY() + (e.serverY - e.getY()) / e.bodyTrackingIncrements;
//			double z = e.getZ() + (e.serverZ - e.getZ()) / e.bodyTrackingIncrements;
//			double yaw = MathHelper.wrapDegrees(e.serverYaw - e.getYaw());
//			e.setYaw(e.getYaw() + (float) yaw / e.bodyTrackingIncrements);
//			e.setPitch((float) (e.getPitch() + (e.serverPitch - e.getPitch()) / e.bodyTrackingIncrements));
//			e.bodyYaw = e.getYaw();
//			--e.bodyTrackingIncrements;
//			e.setPosition(x, y, z);
//		}
//
//		if (e.headTrackingIncrements > 0) {
//			e.headYaw += (float) MathHelper.wrapDegrees(e.serverHeadYaw - (double) e.headYaw) / (float) e.headTrackingIncrements;
//			--e.headTrackingIncrements;
//		}
//	}
}
