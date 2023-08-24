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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
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

	/**
	 * @author Jasmine
	 *
	 * @reason Crimes to squeeze performance out of entity ticking
	 */
	@Overwrite
	public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
		BlockPos pos = this.blockPos;

		// FB: check only for a single chunk loaded
		if (!this.getWorld().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
			return false;
		} else {
			// FB hack: If there's no fluids inside this section, early out
			// This probably breaks stuff. Unfortunately, it also eats performance, so out it goes.

			Chunk chunk = this.getWorld().getChunk(pos);
			if (!chunk.getSection(chunk.getSectionIndex(pos.getY())).hasRandomFluidTicks()) {
				return false;
			}

			Box box = this.getBoundingBox().contract(0.001);
			int i = MathHelper.floor(box.minX);
			int j = MathHelper.ceil(box.maxX);
			int k = MathHelper.floor(box.minY);
			int l = MathHelper.ceil(box.maxY);
			int m = MathHelper.floor(box.minZ);
			int n = MathHelper.ceil(box.maxZ);
			double d = 0.0;
			boolean bl = this.isPushedByFluids();
			boolean bl2 = false;
			Vec3d vec3d = Vec3d.ZERO;
			int o = 0;
			BlockPos.Mutable mutable = new BlockPos.Mutable();

			for(int p = i; p < j; ++p) {
				for(int q = k; q < l; ++q) {
					for(int r = m; r < n; ++r) {
						mutable.set(p, q, r);
						FluidState fluidState = this.getWorld().getFluidState(mutable);
						if (fluidState.isIn(tag)) {
							double e = (double)((float)q + fluidState.getHeight(this.getWorld(), mutable));
							if (e >= box.minY) {
								bl2 = true;
								d = Math.max(e - box.minY, d);
								if (bl) {
									Vec3d vec3d2 = fluidState.getVelocity(this.getWorld(), mutable);
									if (d < 0.4) {
										vec3d2 = vec3d2.multiply(d);
									}

									vec3d = vec3d.add(vec3d2);
									++o;
								}
							}
						}
					}
				}
			}

			if (vec3d.length() > 0.0) {
				if (o > 0) {
					vec3d = vec3d.multiply(1.0 / (double)o);
				}

				if (!(((Entity)(Object)this) instanceof PlayerEntity)) {
					vec3d = vec3d.normalize();
				}

				Vec3d vec3d3 = this.getVelocity();
				vec3d = vec3d.multiply(speed * 1.0);
				double f = 0.003;
				if (Math.abs(vec3d3.x) < 0.003 && Math.abs(vec3d3.z) < 0.003 && vec3d.length() < 0.0045000000000000005) {
					vec3d = vec3d.normalize().multiply(0.0045000000000000005);
				}

				this.setVelocity(this.getVelocity().add(vec3d));
			}

			this.fluidHeight.put(tag, d);
			return bl2;
		}
	}
}
