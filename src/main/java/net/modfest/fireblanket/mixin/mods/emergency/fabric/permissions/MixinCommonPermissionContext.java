package net.modfest.fireblanket.mixin.mods.emergency.fabric.permissions;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.impl.permission.CustomPermissionContext;
import net.fabricmc.fabric.impl.permission.OverriddenPermissionContext;
import net.modfest.fireblanket.mixinsupport.PermissionKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Ampflower
 **/
@Mixin(
	{
		CustomPermissionContext.class,
		OverriddenPermissionContext.class,
	}
)
public class MixinCommonPermissionContext {
	@Inject(method = "set", at = @At("HEAD"))
	private <T> void assertPermissionContext(
		final PermissionContext.Key<T> key,
		final T value,
		final CallbackInfoReturnable<?> ci
	) {
		PermissionKeys.assertValidity(key, value);
	}
}
