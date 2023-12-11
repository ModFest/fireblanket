package net.modfest.fireblanket.world.render_regions;

public interface RegionSubject {

	int fireblanket$getRegionEra();
	boolean fireblanket$getShouldRender();
	long fireblanket$getViewerPos();
	
	void fireblanket$setCachedState(int era, long viewerPos, boolean shouldRender);
	
	default Boolean fireblanket$cachedShouldRender(int era, long viewerPos) {
		if (era == fireblanket$getRegionEra() && fireblanket$getViewerPos() == viewerPos) {
			return fireblanket$getShouldRender();
		}
		return null;
	}
	
}
