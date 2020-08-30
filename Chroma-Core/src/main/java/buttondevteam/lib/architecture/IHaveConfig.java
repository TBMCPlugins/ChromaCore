package buttondevteam.lib.architecture;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import lombok.Getter;
import lombok.Setter;
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
	/**
	 * Returns the Bukkit ConfigurationSection. Use {@link #signalChange()} after changing it.
	 */
	@Getter
	private ConfigurationSection config;
	@Getter
	@Setter
	private Runnable saveAction;

	/**
	 * May be used in testing.
	 *
	 * @param saveAction What to do to save the config to disk. Don't use get methods until it's non-null.
	 */
	IHaveConfig(Runnable saveAction) {
		this.saveAction = saveAction;
	}

	/**
	 * Gets a config object for the given path. The def or primitiveDef must be set. If a getter is present, a setter must be present as well.
	 *
	 * @param path The dot-separated path relative to this config instance
	 * @param <T>  The runtime type of the config value
	 * @return A ConfigData builder to set how to obtain the value
	 */
	public <T> ConfigData.ConfigDataBuilder<T> getConfig(String path) {
		return ConfigData.builder(this, path);
	}

	void onConfigBuild(ConfigData<?> config) {
		datamap.put(config.getPath(), config);
	}

	/**
	 * This method overload should only be used with primitives or String.
	 *
	 * @param path The path in config to use
	 * @param def  The value to use by default
	 * @param <T>  The type of this variable (only use primitives or String)
	 * @return The data object that can be used to get or set the value
	 */
	@SuppressWarnings("unchecked")
	public <T> ConfigData<T> getData(String path, T def) {
		ConfigData<?> data = datamap.get(path);
		if (data == null) datamap.put(path, data = new ConfigData<>(this, path, def, def, null, null));
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
			datamap.put(path, data = new ConfigData<>(this, path, def, setter.apply(def), getter, setter));
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
			datamap.put(path, data = new ConfigData<>(this, path, getter.apply(primitiveDef), primitiveDef, getter, setter));
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
	public <T> ReadOnlyConfigData<T> getReadOnlyDataPrimDef(String path, Object primitiveDef, Function<Object, T> getter, Function<T, Object> setter) {
		ConfigData<?> data = datamap.get(path);
		if (data == null)
			datamap.put(path, data = new ReadOnlyConfigData<>(this, path, getter.apply(primitiveDef), primitiveDef, getter, setter));
		return (ReadOnlyConfigData<T>) data;
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
			datamap.put(path, data = new ConfigData<>(this, path, defval, defval, null, null));
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
			datamap.put(path, data = new ConfigData<>(this, path, defval, setter.apply(defval), getter, setter));
		}
		return (ConfigData<T>) data;
	}

	/**
	 * This method overload should only be used with primitves or String.
	 *
	 * @param path The path in config to use
	 * @param <T>  The type of this variable (only use primitives or String)
	 * @return The data object that can be used to get or set the value
	 */
	@SuppressWarnings("unchecked")
	public <T> ListConfigData<T> getListData(String path) {
		ConfigData<?> data = datamap.get(path);
		if (data == null)
			datamap.put(path, data = new ListConfigData<>(this, path, new ListConfigData.List<T>()));
		return (ListConfigData<T>) data;
	}

	/**
	 * Schedules a save operation. Use after changing the ConfigurationSection directly.
	 */
	public void signalChange() {
		ConfigData.signalChange(this);
	}

	/**
	 * Clears all caches and loads everything from yaml.
	 */
	public void reset(ConfigurationSection config) {
		this.config = config;
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
					if (TBMCCoreAPI.IsTestServer())
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
