package buttondevteam.lib.architecture;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import lombok.Getter;
import lombok.val;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A config system
 */
public final class IHaveConfig {
	private final HashMap<String, ConfigData<?>> datamap = new HashMap<>();
	@Getter
	private ConfigurationSection config;
	private final Runnable saveAction;

	/**
	 * May be used in testing.
	 *
	 * @param section May be null for testing
	 */
	IHaveConfig(ConfigurationSection section, Runnable saveAction) {
		config = section;
		this.saveAction = saveAction;
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
		if (data == null) datamap.put(path, data = new ConfigData<>(config, path, def, def, saveAction));
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
			datamap.put(path, data = new ConfigData<>(config, path, def, setter.apply(def), getter, setter, saveAction));
		return (ConfigData<T>) data;
	}

	/**
	 * This method overload may be used with any class. The given default value will be run through the getter.
	 *
	 * @param path         The path in config to use
	 * @param primitiveDef The <b>primitive</b> value to use by default
	 * @param getter       A function that converts a primitive representation to the correct value
	 * @param setter       A function that converts a value to a primitive representation
	 * @param <T>          The type of this variable (can be any class)
	 * @return The data object that can be used to get or set the value
	 */
	@SuppressWarnings("unchecked")
	public <T> ConfigData<T> getDataPrimDef(String path, Object primitiveDef, Function<Object, T> getter, Function<T, Object> setter) {
		ConfigData<?> data = datamap.get(path);
		if (data == null)
			datamap.put(path, data = new ConfigData<>(config, path, getter.apply(primitiveDef), primitiveDef, getter, setter, saveAction));
		return (ConfigData<T>) data;
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
	public <T> ConfigData<T> getData(String path, Supplier<T> def) {
		ConfigData<?> data = datamap.get(path);
		if (data == null) {
			val defval = def.get();
			datamap.put(path, data = new ConfigData<>(config, path, defval, defval, saveAction));
		}
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
	public <T> ConfigData<T> getData(String path, Supplier<T> def, Function<Object, T> getter, Function<T, Object> setter) {
		ConfigData<?> data = datamap.get(path);
		if (data == null) {
			val defval = def.get();
			datamap.put(path, data = new ConfigData<>(config, path, defval, setter.apply(defval), getter, setter, saveAction));
		}
		return (ConfigData<T>) data;
	}

	/**
	 * Generates the config YAML.
	 *
	 * @param obj       The object which has config methods
	 * @param configMap The result from {@link Component#getConfigMap(String, Map)}. May be null.
	 */
	public static void pregenConfig(Object obj, @Nullable Map<String, IHaveConfig> configMap) {
		val ms = obj.getClass().getDeclaredMethods();
		for (val m : ms) {
			if (!m.getReturnType().getName().equals(ConfigData.class.getName())) continue;
			try {
				m.setAccessible(true);
				List<ConfigData<?>> configList;
				if (m.getParameterCount() == 0) {
					configList = Collections.singletonList((ConfigData<?>) m.invoke(obj));
				} else if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == IHaveConfig.class) {
					if (configMap == null) continue; //Hope it will get called with the param later
					configList = configMap.entrySet().stream().map(kv ->
					{
						try {
							return (ConfigData<?>) m.invoke(obj, kv.getValue());
						} catch (IllegalAccessException | InvocationTargetException e) {
							TBMCCoreAPI.SendException("Failed to pregenerate " + m.getName() + " for " + obj + " using config " + kv.getKey() + "!", e);
							return null;
						}
					}).filter(Objects::nonNull).collect(Collectors.toList());
				} else {
					MainPlugin.Instance.getLogger().warning("Method " + m.getName() + " returns a config but its parameters are unknown: " + Arrays.toString(m.getParameterTypes()));
					continue;
				}
				for (val c : configList) {
					if (c.getPath().length() == 0)
						c.setPath(m.getName());
					else if (!c.getPath().equals(m.getName()))
						MainPlugin.Instance.getLogger().warning("Config name does not match: " + c.getPath() + " instead of " + m.getName());
					c.get(); //Saves the default value if needed - also checks validity
				}
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Failed to pregenerate " + m.getName() + " for " + obj + "!", e);
			}
		}
	}
}
