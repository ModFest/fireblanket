package net.modfest.fireblanket.mixin.entity_perf;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class MixinEntity {
	@Shadow
	public abstract Box getBoundingBox();

	@Shadow
	public abstract boolean isAlive();

	@Shadow
	public abstract World getWorld();

	@Shadow
	protected abstract void onBlockCollision(BlockState state);

	/**
	 * @author jaskarth
	 *
	 * @reason Optimized YXZ order
	 */
	@Overwrite
	public void checkBlockCollision() {
		Box box = this.getBoundingBox();
		BlockPos blockPos = BlockPos.ofFloored(box.minX + 1.0E-7, box.minY + 1.0E-7, box.minZ + 1.0E-7);
		BlockPos blockPos2 = BlockPos.ofFloored(box.maxX - 1.0E-7, box.maxY - 1.0E-7, box.maxZ - 1.0E-7);
		if (this.getWorld().isRegionLoaded(blockPos, blockPos2)) {
			BlockPos.Mutable mutable = new BlockPos.Mutable();

			// YXZ
			for (int y = blockPos.getY(); y <= blockPos2.getY(); y++) {
				for (int x = blockPos.getX(); x <= blockPos2.getX(); x++) {
					for (int z = blockPos.getZ(); z <= blockPos2.getZ(); z++) {
						if (!this.isAlive()) {
							return;
						}

						mutable.set(x, y, z);
						BlockState blockState = this.getWorld().getBlockState(mutable);

						try {
							blockState.onEntityCollision(this.getWorld(), mutable, (Entity) (Object) this);
							this.onBlockCollision(blockState);
						} catch (Throwable var12) {
							CrashReport crashReport = CrashReport.create(var12, "Colliding entity with block");
							CrashReportSection crashReportSection = crashReport.addElement("Block being collided with");
							CrashReportSection.addBlockInfo(crashReportSection, this.getWorld(), mutable, blockState);
							throw new CrashException(crashReport);
						}
					}
				}
			}
		}
	}
}
