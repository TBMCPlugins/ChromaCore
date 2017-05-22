package buttondevteam.lib.chat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>Abstract classes with no {@link CommandClass} annotations will be ignored.</b> Classes that are not abstract or have the annotation will be included in the command path unless
 * {@link #excludeFromPath()} is true.<br>
 * <i>All commands with no modOnly set will <u>not be mod only</u></i>
 * 
 * @author NorbiPeti
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface CommandClass {
	/**
	 * Determines whether the command can only be used by mods and above or regular players can use it as well.<br>
	 * <b>If not set, the command will <u>not</u> be mod only</b>
	 * 
	 * @return If the command is mod only
	 */
	public boolean modOnly() default false;

	/**
	 * The command's path, or name if top-level command.<br>
	 * For example:<br>
	 * "u admin updateplugin" or "u" for the top level one<br>
	 * <u>The path must be lowercase!</u><br>
	 * 
	 * @return The command path, <i>which is the command class name by default</i> (removing any "command" from it)
	 */
	public String path() default "";

	/**
	 * Exclude this class from the path. Useful if more commands share some property but aren't subcommands of a common command. See {@link CommandClass} for more details.
	 */
	public boolean excludeFromPath() default false;
}
