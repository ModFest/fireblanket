package net.modfest.fireblanket.mixin.client.render_regions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.modfest.fireblanket.world.render_regions.RegionSubject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Entity.class, BlockEntity.class})
public class MixinRegionSubjects implements RegionSubject {

	@Unique
	private int fireblanket$regionEra = -1;
	@Unique
	private boolean fireblanket$shouldRender = true;
	@Unique
	private long fireblanket$viewerPos = Long.MIN_VALUE;
	@Unique
	private long fireblanket$targetPos = Long.MIN_VALUE;

	@Override
	public int fireblanket$getRegionEra() {
		return fireblanket$regionEra;
	}

	@Override
	public boolean fireblanket$getShouldRender() {
		return fireblanket$shouldRender;
	}

	@Override
	public long fireblanket$getViewerPos() {
		return fireblanket$viewerPos;
	}

	@Override
	public long fireblanket$getTargetPos() {
		return fireblanket$targetPos;
	}

	@Override
	public void fireblanket$setCachedState(int era, long viewerPos, long targetPos, boolean shouldRender) {
		fireblanket$regionEra = era;
		fireblanket$viewerPos = viewerPos;
		fireblanket$targetPos = targetPos;
		fireblanket$shouldRender = shouldRender;
	}
}
