package net.modfest.fireblanket.mixin.annoyances.lightning;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.modfest.fireblanket.Fireblanket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * @author Ampflower
 **/
@Mixin(ChunkMap.class)
public class MixinServerChunkLoadingManager {
	@Shadow
	@Final
	private ServerLevel level;

	@ModifyVariable(
		method = "addEntity",
		at = @At(
			value = "STORE",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/entity/EntityType;clientTrackingRange()I"
			)
		),
		name = "range"
	)
	private int modifyVariable(int input, @Local(argsOnly = true) Entity entity) {
		if (entity instanceof LightningBolt) {
			final int distance = this.level.getGameRules().get(Fireblanket.LIGHTNING_BROADCAST_RADIUS);
			if (distance >= 0) {
				return distance;
			}
		}
		return input;
	}
}
