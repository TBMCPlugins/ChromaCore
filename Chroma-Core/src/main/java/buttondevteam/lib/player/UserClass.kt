package buttondevteam.lib.player;

import java.lang.annotation.*;

/**
 * Specifies a {@link ChromaGamerBase} direct subclass which can be instantiated. For Minecraft data, use {@link PlayerClass}
 * 
 * @author NorbiPeti
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface UserClass {
	/**
	 * Indicates which folder should the player files be saved in. Must be lowercase.
	 */
	String foldername();
}
