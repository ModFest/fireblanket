package net.modfest.fireblanket.mixinsupport.modifiers;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that a mod's presence breaks a given mixin.
 *
 * @author Ampflower
 **/
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Repeatable(Conflicts.class)
public @interface Conflict {
	/**
	 * The ID of the mod that breaks the mixin.
	 */
	String value();

	/**
	 * The version of the mod that breaks the mixin.
	 */
	@ApiStatus.Experimental
	String version() default "*";

	/**
	 * The reason why it's broken. Leave empty to omit the warning.
	 */
	@ApiStatus.Experimental
	String reason() default "";
}
