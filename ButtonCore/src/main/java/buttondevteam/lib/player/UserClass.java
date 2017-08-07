package buttondevteam.lib.player;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
	 * Indicates which folder should the player files be saved in.
	 */
	String foldername();
}
