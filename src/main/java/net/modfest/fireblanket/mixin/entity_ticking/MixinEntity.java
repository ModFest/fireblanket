package net.modfest.fireblanket.mixin.entity_ticking;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Entity.class, priority = 900)
public abstract class MixinEntity {
	@Shadow
	public abstract boolean touchingUnloadedChunk();

	@Shadow
	private AABB bb;

	@Shadow
	public abstract boolean isPushedByFluid();

	@Shadow
	public abstract AABB getBoundingBox();

	@Shadow
	public abstract Level level();

	@Shadow
	public abstract Vec3 getDeltaMovement();

	@Shadow
	protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

	@Shadow
	public abstract void setDeltaMovement(Vec3 velocity);

	@Shadow
	private BlockPos blockPosition;

	/**
	 * @author Jasmine
	 * @reason Sure hope people aren't using pistons to move entities
	 */
	@Overwrite
	public void checkSupportingBlock(boolean onGround, @Nullable Vec3 movement) {

	}
}
