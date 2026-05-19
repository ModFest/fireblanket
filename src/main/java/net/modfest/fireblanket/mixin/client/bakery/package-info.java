/**
 * @author Ampflower
 */
@NullMarked
@Require(value = "glowcase", version = ">=2.5.0-", reason = "Bakery")
@Conflict(value = "iris", reason = "Shaders don't cooperate with the bakery.")
package net.modfest.fireblanket.mixin.client.bakery;

import net.modfest.fireblanket.mixinsupport.modifiers.Conflict;
import net.modfest.fireblanket.mixinsupport.modifiers.Require;
import org.jspecify.annotations.NullMarked;
