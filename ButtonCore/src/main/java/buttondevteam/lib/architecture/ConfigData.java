package buttondevteam.lib.architecture;

import buttondevteam.core.MainPlugin;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Use the getter/setter constructor if {@link T} isn't a primitive type or String.<br>
 *     Use {@link Component#getConfig()} or {@link ButtonPlugin#getIConfig()} then {@link IHaveConfig#getData(String, Object)} to get an instance.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
//@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ConfigData<T> {
	private static final HashMap<Configuration, BukkitTask> saveTasks= new HashMap<>();
	/**
	 * May be null for testing
	 */
	private final ConfigurationSection config;
	private final String path;
	private final T def;
	private final Object primitiveDef;
	private final Runnable saveAction;
	/**
	 * The parameter is of a primitive type as returned by {@link YamlConfiguration#get(String)}
	 */
	private Function<Object, T> getter;
	/**
	 * The result should be a primitive type or string that can be retrieved correctly later
	 */
	private Function<T, Object> setter;

	/**
	 * The config value should not change outside this instance
	 */
	private T value;
	private boolean saved = false;

	public ConfigData(ConfigurationSection config, String path, T def, Object primitiveDef, Function<Object, T> getter, Function<T, Object> setter, Runnable saveAction) {
		this.config = config;
		this.path = path;
		this.def = def;
		this.primitiveDef = primitiveDef;
		this.getter = getter;
		this.setter = setter;
		this.saveAction=saveAction;
	}

	@SuppressWarnings("unchecked")
	public T get() {
		if (value != null) return value; //Speed things up
		Object val = config == null ? null : config.get(path); //config==null: testing
		if (val == null) {
			val = primitiveDef;
		}
		if (!saved && Objects.equals(val, primitiveDef)) { //String needs .equals()
			if (def == null && config != null) //In Discord's case def may be null
				config.set(path, primitiveDef);
			else
				set(def); //Save default value - def is always set
			saved = true;
		}
		if (getter != null) {
			T hmm = getter.apply(val);
			if (hmm == null) hmm = def; //Set if the getter returned null
			return hmm;
		}
		if (val instanceof Number) {
			if (def instanceof Long)
				val = ((Number) val).longValue();
			else if (def instanceof Short)
				val = ((Number) val).shortValue();
			else if (def instanceof Byte)
				val = ((Number) val).byteValue();
			else if (def instanceof Float)
				val = ((Number) val).floatValue();
			else if (def instanceof Double)
				val = ((Number) val).doubleValue();
		}
		if (val instanceof List && def.getClass().isArray())
			val = ((List<T>) val).toArray((T[]) Array.newInstance(def.getClass().getComponentType(), 0));
		return (T) val;
	}

	public void set(T value) {
		Object val;
		if (setter != null && value != null)
			val = setter.apply(value);
		else val = value;
		if (config != null) {
			config.set(path, val);
			if(!saveTasks.containsKey(config.getRoot())) {
				synchronized (saveTasks) {
					saveTasks.put(config.getRoot(), Bukkit.getScheduler().runTaskLaterAsynchronously(MainPlugin.Instance, () -> {
						synchronized (saveTasks) {
							saveTasks.remove(config.getRoot());
							saveAction.run();
						}
					}, 100));
				}
			}
		}
		this.value = value;
	}
}
