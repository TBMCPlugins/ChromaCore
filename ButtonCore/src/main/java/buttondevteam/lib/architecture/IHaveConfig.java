package buttondevteam.lib.architecture;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.function.Function;

/**
 * Members of this interface should be protected (access level)
 */
final class IHaveConfig {
	private IHaveConfig() {}

	/**
	 * This method overload should only be used with primitves or String.
	 *
	 * @param path The path in config to use
	 * @param def  The value to use by default
	 * @param <T>  The type of this variable (only use primitives or String)
	 * @return The data object that can be used to get or set the value
	 */
	@SuppressWarnings("unchecked")
	protected static <T> ConfigData<T> getData(Map<String, ConfigData<?>> datamap, ConfigurationSection config, String path, T def) {
		ConfigData<?> data = datamap.get(path);
		if (data == null) datamap.put(path, data = new ConfigData<>(config, path, def));
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
	protected static <T> ConfigData<T> getData(Map<String, ConfigData<?>> datamap, ConfigurationSection config, String path, T def, Function<Object, T> getter, Function<T, Object> setter) {
		ConfigData<?> data = datamap.get(path);
		if (data == null) datamap.put(path, data = new ConfigData<>(config, path, def, getter, setter));
		return (ConfigData<T>) data;
	}
}
