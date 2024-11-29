package net.modfest.fireblanket.mixin.adventure_fix;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;
import net.modfest.fireblanket.FireblanketConstants;
import net.modfest.fireblanket.mixinsupport.InteractionCheck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla uses an anonymous class to process entity interaction serverside, so there's not
 * really a better place to do this. Entity#interactAt is only called here,
 * so we might as well filter both.
 *
 * While the client has an easy way to do the check in ClientPlayerInteractionManager,
 * the ServerPlayerInteractionManager does not. /shrug
 */
@Mixin(targets = "net.minecraft.server.network.ServerPlayNetworkHandler$1")
public class MixinPlayerInteractEntityC2SPacketHandler {
	@Shadow @Final private ServerPlayNetworkHandler field_28963; // outer this
	@Shadow @Final private Entity field_28962; // target entity captured variable

	@Inject(
		method = "Lnet/minecraft/server/network/ServerPlayNetworkHandler$1;processInteract(Lnet/minecraft/util/Hand;Lnet/minecraft/server/network/ServerPlayNetworkHandler$Interaction;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void fireblanket$filterEntityInteractByTag(Hand hand, ServerPlayNetworkHandler.Interaction action, CallbackInfo ci) {
		PlayerEntity player = field_28963.player;
		ItemStack stack = player.getStackInHand(hand);
		if (!player.getAbilities().allowModifyWorld &&
			(InteractionCheck.preventUseItem(player, stack) || InteractionCheck.preventUseEntity(player, field_28962.getType()))) {
			ci.cancel();
		}
	}

	@Inject(
		method = "Lnet/minecraft/server/network/ServerPlayNetworkHandler$1;attack()V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void fireblanket$filterAttackEntityByTag(CallbackInfo ci) {
		PlayerEntity player = field_28963.player;
		if (!player.getAbilities().allowModifyWorld && InteractionCheck.preventAttackEntity(player, field_28962.getType())) {
			ci.cancel();
		}
	}
}
