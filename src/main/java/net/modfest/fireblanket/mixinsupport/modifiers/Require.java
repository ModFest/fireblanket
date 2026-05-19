package net.modfest.fireblanket.mixinsupport.modifiers;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes mod requirement for a given mixin.
 *
 * @author Ampflower
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Repeatable(Requires.class)
public @interface Require {
	/**
	 * The ID of the mod that is required.
	 */
	String value();

	/**
	 * The version of the mod that's required.
	 */
	String version() default "*";

	/**
	 * The reason why the mod is required. Leave empty to omit the warning.
	 */
	@ApiStatus.Experimental
	String reason() default "";
}
