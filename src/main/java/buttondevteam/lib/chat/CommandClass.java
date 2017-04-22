package buttondevteam.lib.chat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface CommandClass {
	/**
	 * Determines whether the command can only be used by mods and above or regular players can use it as well.
	 * 
	 * @return If the command is mod only
	 */
	public boolean modOnly();

	/**
	 * The command's path, or name if top-level command.<br>
	 * For example:<br>
	 * "u admin updateplugin" or "u" for the top level one<br>
	 * <u>The path must be lowercase!</u><br>
	 * 
	 * @return The command path, <i>which is the command class name by default</i> (removing any "command" from it)
	 */
	public String path() default "";
}
