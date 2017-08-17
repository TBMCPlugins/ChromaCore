package buttondevteam.lib.chat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
	public boolean playerOnly();
}
