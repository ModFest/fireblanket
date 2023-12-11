package net.modfest.fireblanket.mixin.entity_ticking.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.modfest.fireblanket.world.entity.EntityTick;

@Pseudo
@Mixin(targets="com.simibubi.create.content.contraptions.glue.SuperGlueEntity")
public abstract class MixinSuperGlueEntity extends Entity {

	public MixinSuperGlueEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Redirect(method="method_5773", at=@At(value="INVOKE", target="net/minecraft/entity/Entity.tick()V"))
	private void fireblanket$noTick(@Coerce Entity instance) {
		EntityTick.minimalTick(instance);
	}
}
