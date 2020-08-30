package buttondevteam.lib.architecture;

import java.util.function.Function;

public class ReadOnlyConfigData<T> extends ConfigData<T> {
	ReadOnlyConfigData(IHaveConfig config, String path, T def, Object primitiveDef, Function<Object, T> getter, Function<T, Object> setter) {
		super(config, path, def, primitiveDef, getter, setter);
	}

	ReadOnlyConfigData(IHaveConfig config, String path, T def, Object primitiveDef) {
		super(config, path, def, primitiveDef, null, null);
	}
}
