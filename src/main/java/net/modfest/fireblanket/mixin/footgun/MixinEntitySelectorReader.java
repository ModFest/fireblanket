package net.modfest.fireblanket.mixin.footgun;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.modfest.fireblanket.mixinsupport.ForceableArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/**
 * Prevents foot-gunning by using an unlimited @e selector without forcing to assure you know what you're doing
 */
@Mixin(EntitySelectorParser.class)
public class MixinEntitySelectorReader implements ForceableArgument {
	@Shadow
	private boolean includesEntities;
	@Shadow
	private int maxResults;
	@Shadow
	private MinMaxBounds.Doubles distance;
	@Shadow
	private Double deltaX;
	@Shadow
	private Double deltaY;
	@Shadow
	private Double deltaZ;
	private boolean forced = false;

	private static final DynamicCommandExceptionType LIMIT_UNFORCED = new DynamicCommandExceptionType(
		count -> Component.translatableEscape("argument.entity.selector.limit.unforced", count)
	);

	@Override
	public void setForced(boolean forced) {
		this.forced = forced;
	}

	@Override
	public boolean isForced() {
		return forced;
	}

	@Inject(method = "parse", at = @At("RETURN"))
	private void fireblanket$preventFootgun(CallbackInfoReturnable<EntitySelector> info) throws CommandSyntaxException {
		if (this.includesEntities
			//main anti-footgun: don't allow someone to affect every single entity on the server at once
			&& (this.maxResults > 50 && this.distance == MinMaxBounds.Doubles.ANY)
			&& (this.deltaX == null && this.deltaY == null && this.deltaZ == null)
			&& !forced) {
			throw LIMIT_UNFORCED.create(this.maxResults);
		}
	}

	@Inject(method = "addPredicate", at = @At("HEAD"))
	private void fireblanket$forceWithPredicate(Predicate<Entity> predicate, CallbackInfo info) {
		//predicates are a Limiting Factor so it should be good if anything sets them
		this.forced = true;
	}
}
