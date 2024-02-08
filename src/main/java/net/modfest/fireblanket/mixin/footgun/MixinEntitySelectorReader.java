package net.modfest.fireblanket.mixin.footgun;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.predicate.NumberRange;
import net.minecraft.text.Text;
import net.modfest.fireblanket.mixinsupport.ForceableArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents foot-gunning by using an unlimited @e selector without forcing to assure you know what you're doing
 */
@Mixin(EntitySelectorReader.class)
public class MixinEntitySelectorReader implements ForceableArgument {
	@Shadow private boolean includesNonPlayers;
	@Shadow private int limit;
	@Shadow private NumberRange.DoubleRange distance;
	private boolean forced = false;

	private static final DynamicCommandExceptionType LIMIT_UNFORCED = new DynamicCommandExceptionType(
			count -> Text.stringifiedTranslatable("argument.entity.selector.limit.unforced", count)
	);

	@Override
	public void setForced(boolean forced) {
		this.forced = forced;
	}

	@Override
	public boolean isForced() {
		return forced;
	}

	@Inject(method = "read", at = @At("RETURN"))
	private void preventFootgun(CallbackInfoReturnable<EntitySelector> info) throws CommandSyntaxException {
		if (this.includesNonPlayers
				&& (this.limit > 50  || this.distance == NumberRange.DoubleRange.ANY)
				&& !forced) {
			throw LIMIT_UNFORCED.create(this.limit);
		}
	}
}
