package buttondevteam.lib.chat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The method must return with {@link String}[] or {@link Iterable}&lt;{@link String}&gt; and may have the sender and preceding arguments as parameters.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomTabCompleteMethod {
	/**
	 * The parameter's name where we want to give completion
	 */
	String param();

	/**
	 * The subcommand(s) which have the parameter, by default the method's name
	 */
	String[] subcommand() default "";
}
