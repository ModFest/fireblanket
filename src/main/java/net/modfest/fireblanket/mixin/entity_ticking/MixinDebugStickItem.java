package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DebugStickItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;
import net.modfest.fireblanket.mixinsupport.NonVehicleEnteringLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(DebugStickItem.class)
public class MixinDebugStickItem extends Item {
	@Unique
	private static final String NOAI = "NoAI";
	@Unique
	private static final String NOGRAV = "NoGravity";
	@Unique
	private static final String NOMOV = "NoMovement";
	@Unique
	private static final String NOVEHICLE = "NoVehicleEntering";

	public MixinDebugStickItem(Properties settings) {
		super(settings);
	}

	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void fireblanket$dontApplyCustom(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (isCustomFireblanket(context.getItemInHand().getComponents().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag())) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
		CompoundTag nbt = stack.getComponents().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (isCustomFireblanket(nbt)) {
			// Only builder+ should be able to use debug hammers
			if (!user.level().isClientSide() && Roles.isBuilder(user)) {
				if (nbt.getBooleanOr(NOAI, false)) {
					if (entity instanceof Mob mob) {
						mob.setNoAi(true);
					} else {
						user.sendSystemMessage(Component.literal("This entity isn't a mob, and can't have NoAI applied!"));
						return InteractionResult.PASS;
					}
				}

				if (nbt.getBooleanOr(NOVEHICLE, false)) {
					if (!(entity instanceof Player)) {
						((NonVehicleEnteringLivingEntity) entity).fireblanket$setNoVehicleEntering(true);
					} else {
						user.sendSystemMessage(Component.literal("This entity is a player, and can't have NoVehicleEntering applied!"));
						return InteractionResult.PASS;
					}
				}

				if (nbt.getBooleanOr(NOGRAV, false)) {
					entity.setNoGravity(true);
				}

				if (nbt.getBooleanOr(NOMOV, false)) {
					((ImmmovableLivingEntity) entity).fireblanket$setNoMovement(true);
				}

				user.sendSystemMessage(Component.literal("Successfully applied."));
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	public Component getName(ItemStack stack) {
		if (isCustomFireblanket(stack.getComponents().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag())) {
			return Component.literal("[Fireblanket] Debug Hammer");
		}

		return super.getName(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
		CompoundTag nbt = stack.getComponents().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

		if (isCustomFireblanket(nbt)) {
			tooltip.accept(Component.literal(ChatFormatting.RED + "This debug stick can't edit blocks!"));
			tooltip.accept(Component.literal(ChatFormatting.LIGHT_PURPLE + "Instead, it:"));

			if (nbt.getBooleanOr(NOAI, false)) {
				tooltip.accept(Component.literal(ChatFormatting.LIGHT_PURPLE + "- Makes mob entities not have AI"));
			}

			if (nbt.getBooleanOr(NOGRAV, false)) {
				tooltip.accept(Component.literal(ChatFormatting.LIGHT_PURPLE + "- Makes living entities not have gravity"));
			}

			if (nbt.getBooleanOr(NOMOV, false)) {
				tooltip.accept(Component.literal(ChatFormatting.LIGHT_PURPLE + "- Makes living entities not attempt movement at all"));
			}

			if (nbt.getBooleanOr(NOVEHICLE, false)) {
				tooltip.accept(Component.literal(ChatFormatting.LIGHT_PURPLE + "- Makes living entities not enter vehicles upon collision"));
			}
		}
	}

	@Unique
	private static boolean isCustomFireblanket(CompoundTag nbt) {
		return nbt.getBooleanOr(NOAI, false) || nbt.getBooleanOr(NOGRAV, false) || nbt.getBooleanOr(NOMOV, false) || nbt.getBooleanOr(NOVEHICLE, false);
	}
}
