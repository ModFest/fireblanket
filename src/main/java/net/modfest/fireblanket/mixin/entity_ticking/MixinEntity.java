package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Entity.class, priority = 900)
public abstract class MixinEntity {

	/**
	 * @author Jasmine
	 * @reason Sure hope people aren't using pistons to move entities
	 */
	@Overwrite
	public void checkSupportingBlock(boolean onGround, @Nullable Vec3 movement) {

	}
}
