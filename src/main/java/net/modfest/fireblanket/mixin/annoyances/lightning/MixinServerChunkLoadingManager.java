package net.modfest.fireblanket.mixin.annoyances.lightning;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
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
@Mixin(ServerChunkLoadingManager.class)
public class MixinServerChunkLoadingManager {
	@Shadow
	@Final
	ServerWorld world;

	@ModifyVariable(
		method = "loadEntity",
		at = @At(
			value = "STORE",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/entity/EntityType;getMaxTrackDistance()I"
			)
		)
	)
	private int modifyVariable(int input, @Local(argsOnly = true) Entity entity) {
		if (entity instanceof LightningEntity) {
			final int distance = this.world.getGameRules().getInt(Fireblanket.LIGHTNING_BROADCAST_RADIUS);
			if (distance >= 0) {
				return distance;
			}
		}
		return input;
	}
}
