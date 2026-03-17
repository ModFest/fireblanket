package net.modfest.fireblanket.mixin.adventure_fix;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public class MixinPlayerInteractEntityC2SPacketHandler {
	@Shadow
	@Final
	private ServerGamePacketListenerImpl field_28963; // outer this
	@Shadow
	@Final
	private Entity val$target; // target entity captured variable

	@Inject(
		method = "performInteraction(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/server/network/ServerGamePacketListenerImpl$EntityInteraction;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void fireblanket$filterEntityInteractByTag(InteractionHand hand, ServerGamePacketListenerImpl.EntityInteraction action, CallbackInfo ci) {
		Player player = field_28963.player;
		ItemStack stack = player.getItemInHand(hand);
		if (!player.getAbilities().mayBuild &&
			(InteractionCheck.preventUseItem(player, stack) || InteractionCheck.preventUseEntity(player, val$target.getType()))) {
			ci.cancel();
		}
	}

	@Inject(
		method = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl$1;attack()V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void fireblanket$filterAttackEntityByTag(CallbackInfo ci) {
		Player player = field_28963.player;
		if (!player.getAbilities().mayBuild && InteractionCheck.preventAttackEntity(player, val$target.getType())) {
			ci.cancel();
		}
	}
}
