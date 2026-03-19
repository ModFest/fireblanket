package net.modfest.fireblanket.mixin.adventure_fix;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.modfest.fireblanket.mixinsupport.InteractionCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Entity#interactAt is only called here, so we might as well filter both.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class MixinPlayerInteractEntityC2SPacketHandler {
	@Shadow
	public ServerPlayer player;

	@Definition(id = "getEntityOrPart", method = "Lnet/minecraft/server/level/ServerLevel;getEntityOrPart(I)Lnet/minecraft/world/entity/Entity;")
	@Expression("? = ?.getEntityOrPart(?)")
	@Inject(
		method = "handleInteract",
		at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER),
		cancellable = true
	)
	private void fireblanket$filterEntityInteractByTag(
		final ServerboundInteractPacket packet,
		final CallbackInfo ci,
		final @Local Entity target
	) {
		ItemStack stack = player.getItemInHand(packet.hand());
		if (!player.getAbilities().mayBuild &&
			(InteractionCheck.preventUseItem(player, stack) || InteractionCheck.preventUseEntity(player, target))) {
			ci.cancel();
		}
	}

	@Definition(id = "getEntityOrPart", method = "Lnet/minecraft/server/level/ServerLevel;getEntityOrPart(I)Lnet/minecraft/world/entity/Entity;")
	@Expression("? = ?.getEntityOrPart(?)")
	@Inject(
		method = "handleAttack",
		at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER),
		cancellable = true
	)
	private void fireblanket$filterAttackEntityByTag(
		final ServerboundAttackPacket packet,
		final CallbackInfo ci,
		final @Local Entity target
	) {
		if (!player.getAbilities().mayBuild && InteractionCheck.preventAttackEntity(player, target)) {
			ci.cancel();
		}
	}
}
