package net.modfest.fireblanket.mixin.mods.create;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.modfest.fireblanket.world.entity.EntityTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.contraptions.glue.SuperGlueEntity")
public abstract class MixinSuperGlueEntity extends Entity {

	public MixinSuperGlueEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
	private void fireblanket$noTick(@Coerce Entity instance) {
		EntityTick.minimalTick(instance);
	}
}
