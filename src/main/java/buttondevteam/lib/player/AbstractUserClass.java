package buttondevteam.lib.player;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a {@link ChromaGamerBase} direct subclass which's abstract. For Minecraft data, use {@link PlayerClass}
 * 
 * @author NorbiPeti
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AbstractUserClass {
	/**
	 * Indicates which folder should the player files be saved in.
	 */
	String foldername();

	/**
	 * Indicates the class to create when connecting accounts.
	 */
	Class<?> prototype();
}