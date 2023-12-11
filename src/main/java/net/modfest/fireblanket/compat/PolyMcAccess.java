package net.modfest.fireblanket.compat;

import java.util.function.BooleanSupplier;

public class PolyMcAccess {

	public static BooleanSupplier isActive = () -> false;
	
	public static boolean isActive() {
		return isActive.getAsBoolean();
	}
	
}
