package buttondevteam.lib.architecture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentMetadata {
	Class<? extends Component>[] depends() default {};

	boolean enabledByDefault() default true;
}
