package buttondevteam.lib.architecture;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Members of this interface should be protected (access level)
 */
public final class IHaveConfig {
	private final HashMap<String, ConfigData<?>> datamap = new HashMap<>();
	@Getter
	private ConfigurationSection config;

	/**
	 * May be used in testing
	 *
	 * @param section May be null for testing
	 */
	IHaveConfig(ConfigurationSection section) {
		config = section;
	}

	/**
	 * This method overload should only be used with primitves or String.
	 *
	 * @param path The path in config to use
	 * @param def  The value to use by default
	 * @param <T>  The type of this variable (only use primitives or String)
	 * @return The data object that can be used to get or set the value
	 */
	@SuppressWarnings("unchecked")
	public <T> ConfigData<T> getData(String path, T def) {
		ConfigData<?> data = datamap.get(path);
		if (data == null) datamap.put(path, data = new ConfigData<>(config, path, def, def));
		return (ConfigData<T>) data;
	}

	/**
	 * This method overload may be used with any class.
	 *
	 * @param path   The path in config to use
	 * @param def    The value to use by default
	 * @param getter A function that converts a primitive representation to the correct value
	 * @param setter A function that converts a value to a primitive representation
	 * @param <T>    The type of this variable (can be any class)
	 * @return The data object that can be used to get or set the value
	 */
	@SuppressWarnings("unchecked")
	public <T> ConfigData<T> getData(String path, T def, Function<Object, T> getter, Function<T, Object> setter) {
		ConfigData<?> data = datamap.get(path);
		if (data == null)
			datamap.put(path, data = new ConfigData<>(config, path, def, setter.apply(def), getter, setter));
		return (ConfigData<T>) data;
	}

	/**
	 * This method overload may be used with any class. The given default value will be run through the getter.
	 *
	 * @param path   The path in config to use
	 * @param primitiveDef    The <b>primitive</b> value to use by default
	 * @param getter A function that converts a primitive representation to the correct value
	 * @param setter A function that converts a value to a primitive representation
	 * @param <T>    The type of this variable (can be any class)
	 * @return The data object that can be used to get or set the value
	 */
	@SuppressWarnings("unchecked")
	public <T> ConfigData<T> getDataPrimDef(String path, Object primitiveDef, Function<Object, T> getter, Function<T, Object> setter) {
		ConfigData<?> data = datamap.get(path);
		if (data == null)
			datamap.put(path, data = new ConfigData<>(config, path, getter.apply(primitiveDef), primitiveDef, getter, setter));
		return (ConfigData<T>) data;
	}
}
