package buttondevteam.lib.chat;

import java.lang.annotation.*;

/**
 * Only needed to use with {@link OptionallyPlayerCommandBase} command classes
 * 
 * @author NorbiPeti
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface OptionallyPlayerCommandClass {
    boolean playerOnly();
}
