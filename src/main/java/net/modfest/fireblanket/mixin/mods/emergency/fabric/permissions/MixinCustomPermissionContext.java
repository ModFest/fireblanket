package net.modfest.fireblanket.mixin.mods.emergency.fabric.permissions;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.impl.permission.CustomPermissionContext;
import net.modfest.fireblanket.mixinsupport.PermissionKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * @author Ampflower
 **/
@Mixin(CustomPermissionContext.class)
public class MixinCustomPermissionContext {
	@Inject(
		method = "<init>(Ljava/util/UUID;Lnet/fabricmc/fabric/api/permission/v1/PermissionContext$Type;Lnet/minecraft/server/permissions/PermissionLevel;Ljava/util/Map;)V",
		at = @At("HEAD")
	)
	private static void assertPermissionContext(
		final CallbackInfo ci,
		final @Local(argsOnly = true) Map<PermissionContext.Key<?>, ?> map
	) {
		PermissionKeys.assertValidity(map);
	}
}
