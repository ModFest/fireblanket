package net.modfest.fireblanket.mixin.entity_immutability;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.context.UseOnContext;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.util.ImmutableEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HangingEntityItem.class)
public class MixinDecorationItem {
	@Inject(
		method = "useOn",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/HangingEntity;playPlacementSound()V"))
	private void onInitSpawnedEntity(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local HangingEntity entity) {
		if (context.getLevel() instanceof ServerLevel serverWorld && serverWorld.getGameRules().get(Fireblanket.NEW_ENTITIES_IMMUTABLE)) {
			ImmutableEntities.makeImmutable(entity);
		}
	}
}
