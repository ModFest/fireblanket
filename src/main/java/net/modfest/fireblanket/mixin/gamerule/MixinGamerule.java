package net.modfest.fireblanket.mixin.gamerule;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.gamerules.GameRule;
import net.modfest.fireblanket.command.CommandUtils;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public class MixinGamerule {

	@Inject(at = @At("HEAD"), method = "setRule", cancellable = true)
	private static <T> void lockGamerule(
		final CommandContext<CommandSourceStack> context,
		final GameRule<T> gameRule,
		final CallbackInfoReturnable<Integer> cir
	) {
		if (FireblanketConfig.get(ConfigSpecs.LOCKED_GAMERULES).contains(gameRule.id()) && !CommandUtils.isOrganizer(context.getSource())) {
			context.getSource().sendFailure(Component.translatable("commands.gamerule.locked"));
			cir.setReturnValue(0);
		}
	}
}
