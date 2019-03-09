package buttondevteam.lib.architecture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * Used to generate documentation for the config
 */
@Target(ElementType.TYPE)
@Inherited
public @interface HasConfig {
}
