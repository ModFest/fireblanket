package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DebugStickItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(DebugStickItem.class)
public class MixinDebugStickItem extends Item {
	private static final String NOAI = "NoAI";
	private static final String NOGRAV = "NoGravity";
	private static final String NOMOV = "NoMovement";

	public MixinDebugStickItem(Settings settings) {
		super(settings);
	}

	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void fireblanket$dontApplyCustom(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		if (isCustomFireblanket(context.getStack().getComponents().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt())) {
			cir.setReturnValue(ActionResult.PASS);
		}
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		NbtCompound nbt = stack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		if (isCustomFireblanket(nbt)) {
			// Only builder+ should be able to use debug hammers
			if (!user.getWorld().isClient && Roles.isBuilder(user)) {
				if (nbt.getBoolean(NOAI, false)) {
					if (entity instanceof MobEntity mob) {
						mob.setAiDisabled(true);
					} else {
						user.sendMessage(Text.literal("This entity isn't a mob, and can't have NoAI applied!"), true);
						return ActionResult.PASS;
					}
				}

				if (nbt.getBoolean(NOGRAV, false)) {
					entity.setNoGravity(true);
				}

				if (nbt.getBoolean(NOMOV, false)) {
					((ImmmovableLivingEntity) entity).setNoMovement(true);
				}

				user.sendMessage(Text.literal("Successfully applied."), true);
			}
		}

		return ActionResult.PASS;
	}

	@Override
	public Text getName(ItemStack stack) {
		if (isCustomFireblanket(stack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt())) {
			return Text.literal("[Fireblanket] Debug Hammer");
		}

		return super.getName(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> tooltip, TooltipType type) {
		NbtCompound nbt = stack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

		if (isCustomFireblanket(nbt)) {
			tooltip.accept(Text.literal(Formatting.RED + "This debug stick can't edit blocks!"));
			tooltip.accept(Text.literal(Formatting.LIGHT_PURPLE + "Instead, it:"));

			if (nbt.getBoolean(NOAI, false)) {
				tooltip.accept(Text.literal(Formatting.LIGHT_PURPLE + "- Makes mob entities not have AI"));
			}

			if (nbt.getBoolean(NOGRAV, false)) {
				tooltip.accept(Text.literal(Formatting.LIGHT_PURPLE + "- Makes living entities not have gravity"));
			}

			if (nbt.getBoolean(NOMOV, false)) {
				tooltip.accept(Text.literal(Formatting.LIGHT_PURPLE + "- Makes living entities not attempt movement at all"));
			}
		}
	}

	private static boolean isCustomFireblanket(NbtCompound nbt) {
		return nbt.getBoolean(NOAI, false) || nbt.getBoolean(NOGRAV, false) || nbt.getBoolean(NOMOV, false);
	}
}
