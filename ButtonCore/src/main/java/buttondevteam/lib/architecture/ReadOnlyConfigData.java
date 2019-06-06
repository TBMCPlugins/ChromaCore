package buttondevteam.lib.architecture;

import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Function;

public class ReadOnlyConfigData<T> extends ConfigData<T> {
	ReadOnlyConfigData(ConfigurationSection config, String path, T def, Object primitiveDef, Function<Object, T> getter, Function<T, Object> setter, Runnable saveAction) {
		super(config, path, def, primitiveDef, getter, setter, saveAction);
	}

	ReadOnlyConfigData(ConfigurationSection config, String path, T def, Object primitiveDef, Runnable saveAction) {
		super(config, path, def, primitiveDef, saveAction);
	}
}
