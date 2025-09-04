package net.modfest.fireblanket.world.render_regions;

public interface RegionSubject {

	int fireblanket$getRegionEra();

	boolean fireblanket$getShouldRender();

	long fireblanket$getViewerPos();

	long fireblanket$getTargetPos();

	void fireblanket$setCachedState(int era, long viewerPos, long targetPos, boolean shouldRender);

	default Boolean fireblanket$cachedShouldRender(int era, long viewerPos, long targetPos) {
		if (era == fireblanket$getRegionEra()
			&& fireblanket$getViewerPos() == viewerPos
			&& fireblanket$getTargetPos() == targetPos
		) {
			return fireblanket$getShouldRender();
		}
		return null;
	}

}
