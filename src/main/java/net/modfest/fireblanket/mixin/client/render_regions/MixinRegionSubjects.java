package net.modfest.fireblanket.mixin.client.render_regions;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.modfest.fireblanket.render_regions.RegionSubject;

@Mixin({Entity.class, BlockEntity.class})
public class MixinRegionSubjects implements RegionSubject {

	private int fireblanket$regionEra = -1;
	private boolean fireblanket$shouldRender = true;
	private long fireblanket$viewerPos = Long.MIN_VALUE;
	
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
	public void fireblanket$setCachedState(int era, long viewerPos, boolean shouldRender) {
		fireblanket$regionEra = era;
		fireblanket$viewerPos = viewerPos;
		fireblanket$shouldRender = shouldRender;
	}

	
	
}
