package net.modfest.fireblanket.mixin.adventure_fix;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ServerExplosion;
import net.modfest.fireblanket.Fireblanket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Ampflower
 **/
@Mixin(ServerExplosion.class)
public abstract class MixinServerExplosion implements Explosion {
	@ModifyReturnValue(method = "canTriggerBlocks", at = @At("RETURN"))
	private boolean modifyReturn(final boolean original) {
		if (original && this.getIndirectSourceEntity() instanceof ServerPlayer player) {
			return player.gameMode() != GameType.ADVENTURE
				   || this.level().getGameRules().get(Fireblanket.ADVENTURE_WIND_CHARGE_INTERACTION);
		}
		return original;
	}
}
