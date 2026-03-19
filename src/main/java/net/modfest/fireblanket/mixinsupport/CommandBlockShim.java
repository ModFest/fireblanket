package net.modfest.fireblanket.mixinsupport;

import net.minecraft.network.chat.Component;

/**
 * @author Ampflower
 */
public interface CommandBlockShim {
	boolean fireblanket$setLastOutput(final Component message);
}
