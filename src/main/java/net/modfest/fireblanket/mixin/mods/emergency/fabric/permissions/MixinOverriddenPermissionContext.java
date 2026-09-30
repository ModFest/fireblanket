package net.modfest.fireblanket.mixin.mods.emergency.fabric.permissions;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.impl.permission.OverriddenPermissionContext;
import net.modfest.fireblanket.mixinsupport.PermissionKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * @author Ampflower
 **/
@Mixin(OverriddenPermissionContext.class)
public class MixinOverriddenPermissionContext {
	@Inject(
		method = "<init>(Lnet/fabricmc/fabric/api/permission/v1/PermissionContext;Ljava/util/Map;)V",
		at = @At("HEAD")
	)
	private static void assertPermissionContext(
		final PermissionContext context,
		final Map<PermissionContext.Key<?>, ?> map,
		final CallbackInfo ci
	) {
		PermissionKeys.assertValidity(context);
		PermissionKeys.assertValidity(map);
	}
}
