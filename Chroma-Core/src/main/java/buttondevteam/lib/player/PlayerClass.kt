package buttondevteam.lib.player;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a {@link TBMCPlayerBase} direct subclass. For Minecraft data, use {@link UserClass}
 * 
 * @author NorbiPeti
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PlayerClass {
	/**
	 * Indicates the plugin's name which this player class belongs to. Used to create a section for each plugin.
	 */
	String pluginname();
}
