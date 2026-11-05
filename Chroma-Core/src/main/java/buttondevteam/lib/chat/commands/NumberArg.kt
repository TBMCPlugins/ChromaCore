package buttondevteam.lib.chat.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A command argument that can have a number as a value.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberArg {
	/**
	 * The highest value that can be used for this argument.
	 */
	double upperLimit() default Double.POSITIVE_INFINITY;

	/**
	 * The lowest value that can be used for this argument.
	 */
	double lowerLimit() default Double.NEGATIVE_INFINITY;
}
