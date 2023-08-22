package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DebugStickItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
		if (isCustomFireblanket(context.getStack().getOrCreateNbt())) {
			cir.setReturnValue(ActionResult.PASS);
		}
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (isCustomFireblanket(nbt)) {
			if (!user.getWorld().isClient) {
				if (nbt.getBoolean(NOAI)) {
					if (entity instanceof MobEntity mob) {
						mob.setAiDisabled(true);
					} else {
						user.sendMessage(Text.literal("This entity isn't a mob, and can't have NoAI applied!"));
						return ActionResult.PASS;
					}
				}

				if (nbt.getBoolean(NOGRAV)) {
					entity.setNoGravity(true);
				}

				if (nbt.getBoolean(NOMOV)) {
					((ImmmovableLivingEntity)entity).setNoMovement(true);
				}

				user.sendMessage(Text.literal("Successfully applied."), true);
			}
		}

		return ActionResult.PASS;
	}

	@Override
	public Text getName(ItemStack stack) {
		if (stack.hasNbt()) {
			if (isCustomFireblanket(stack.getNbt())) {
				return Text.literal("[Fireblanket] Debug Hammer");
			}
		}

		return super.getName(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null) {
			return;
		}

		if (isCustomFireblanket(nbt)) {
			tooltip.add(Text.literal(Formatting.RED + "This debug stick can't edit blocks!"));
			tooltip.add(Text.literal(Formatting.LIGHT_PURPLE + "Instead, it:"));

			if (nbt.getBoolean(NOAI)) {
				tooltip.add(Text.literal(Formatting.LIGHT_PURPLE + "- Makes mob entities not have AI"));
			}

			if (nbt.getBoolean(NOGRAV)) {
				tooltip.add(Text.literal(Formatting.LIGHT_PURPLE + "- Makes living entities not have gravity"));
			}

			if (nbt.getBoolean(NOMOV)) {
				tooltip.add(Text.literal(Formatting.LIGHT_PURPLE + "- Makes living entities not attempt movement at all"));
			}
		}
	}

	private static boolean isCustomFireblanket(NbtCompound nbt) {
		return nbt.getBoolean(NOAI) || nbt.getBoolean(NOGRAV) || nbt.getBoolean(NOMOV);
	}
}
