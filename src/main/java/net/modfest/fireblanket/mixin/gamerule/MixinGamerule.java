package net.modfest.fireblanket.mixin.gamerule;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.modfest.fireblanket.command.CommandUtils;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public class MixinGamerule {

	@Inject(at = @At("HEAD"), method = "executeSet", cancellable = true)
	private static <T extends GameRules.Rule<T>> void lockGamerule(CommandContext<ServerCommandSource> context, GameRules.Key<T> key, CallbackInfoReturnable<Integer> cir) {
		if (FireblanketConfig.get(ConfigSpecs.LOCKED_GAMERULES).contains(key.getName()) && !CommandUtils.isOrganizer(context.getSource())) {
			context.getSource().sendError(Text.translatable("commands.gamerule.locked"));
			cir.setReturnValue(0);
		}
	}
}
