package net.modfest.fireblanket.mixin.entity_ticking;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Entity.class, priority = 900)
public abstract class MixinEntity {
	@Shadow public abstract boolean isRegionUnloaded();

	@Shadow private Box boundingBox;

	@Shadow public abstract boolean isPushedByFluids();

	@Shadow public abstract Box getBoundingBox();

	@Shadow public abstract World getWorld();

	@Shadow public abstract Vec3d getVelocity();

	@Shadow protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

	@Shadow public abstract void setVelocity(Vec3d velocity);

	@Shadow private BlockPos blockPos;

	@Shadow public abstract void readNbt(NbtCompound nbt);

	/**
	 * @author Jasmine
	 *
	 * @reason Sure hope people aren't using pistons to move entities
	 */
	@Overwrite
	public void updateSupportingBlockPos(boolean onGround, @Nullable Vec3d movement) {

	}
}
