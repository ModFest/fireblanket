package net.modfest.fireblanket.mixin.client.hooks;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.modfest.fireblanket.client.FireblanketDebug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Ampflower
 **/
@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Definition(id = "debugAction", local = @Local(type = boolean.class, name = "debugAction"))
	@Expression("return debugAction")
	@ModifyReturnValue(method = "handleDebugKeys", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean handleFireblanketKeys(final boolean original, @Local(argsOnly = true) KeyEvent event) {
		return original | FireblanketDebug.handleDebugKeys(this.minecraft, event);
	}
}
