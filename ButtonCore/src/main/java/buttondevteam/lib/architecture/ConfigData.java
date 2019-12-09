package buttondevteam.lib.architecture;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.ChromaUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Use the getter/setter constructor if {@link T} isn't a primitive type or String.<br>
 * Use {@link Component#getConfig()} or {@link ButtonPlugin#getIConfig()} then {@link IHaveConfig#getData(String, Object)} to get an instance.
 */
//@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ConfigData<T> {
	private static final HashMap<Configuration, SaveTask> saveTasks = new HashMap<>();
	/**
	 * May be null for testing
	 */
	private final ConfigurationSection config;
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private String path;
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

	//This constructor is needed because it sets the getter and setter
	ConfigData(ConfigurationSection config, String path, T def, Object primitiveDef, Function<Object, T> getter, Function<T, Object> setter, Runnable saveAction) {
		this.config = config;
		this.path = path;
		this.def = def;
		this.primitiveDef = primitiveDef;
		this.getter = getter;
		this.setter = setter;
		this.saveAction = saveAction;
	}

	@java.beans.ConstructorProperties({"config", "path", "def", "primitiveDef", "saveAction"})
	ConfigData(ConfigurationSection config, String path, T def, Object primitiveDef, Runnable saveAction) {
		this.config = config;
		this.path = path;
		this.def = def;
		this.primitiveDef = primitiveDef;
		this.saveAction = saveAction;
	}

	@Override
	public String toString() {
		return "ConfigData{" +
			"path='" + path + '\'' +
			", value=" + value +
			'}';
	}

	@SuppressWarnings("unchecked")
	public T get() {
		if (value != null) return value; //Speed things up
		Object val;
		if (config == null || !config.isSet(path)) { //Call set() if config == null
			val = primitiveDef;
			if ((def == null || this instanceof ReadOnlyConfigData) && config != null) //In Discord's case def may be null
				setInternal(primitiveDef); //If read-only then we still need to save the default value so it can be set
			else
				set(def); //Save default value - def is always set
		} else
			val = config.get(path); //config==null: testing
		if (val == null) //If it's set to null explicitly
			val = primitiveDef;
		BiFunction<Object, Object, Object> convert = (_val, _def) -> {
			if (_def instanceof Number) //If we expect a number
				if (_val instanceof Number)
					_val = ChromaUtils.convertNumber((Number) _val,
						(Class<? extends Number>) _def.getClass());
				else
					_val = _def; //If we didn't get a number, return default (which is a number)
			else if (_val instanceof List && _def != null && _def.getClass().isArray())
				_val = ((List<T>) _val).toArray((T[]) Array.newInstance(_def.getClass().getComponentType(), 0));
			return _val;
		};
		if (getter != null) {
			val = convert.apply(val, primitiveDef);
			T hmm = getter.apply(val);
			if (hmm == null) hmm = def; //Set if the getter returned null
			return hmm;
		}
		val = convert.apply(val, def);
		return value = (T) val; //Always cache, if not cached yet
	}

	public void set(T value) {
		if (this instanceof ReadOnlyConfigData)
			return; //Safety for Discord channel/role data
		Object val;
		if (setter != null && value != null)
			val = setter.apply(value);
		else val = value;
		if (config != null)
			setInternal(val);
		this.value = value;
	}

	private void setInternal(Object val) {
		config.set(path, val);
		if (!saveTasks.containsKey(config.getRoot())) {
			synchronized (saveTasks) {
				saveTasks.put(config.getRoot(), new SaveTask(Bukkit.getScheduler().runTaskLaterAsynchronously(MainPlugin.Instance, () -> {
					synchronized (saveTasks) {
						saveTasks.remove(config.getRoot());
						saveAction.run();
					}
				}, 100), saveAction));
			}
		}
	}

	@AllArgsConstructor
	private static class SaveTask {
		BukkitTask task;
		Runnable saveAction;
	}

	public static boolean saveNow(Configuration config) {
		SaveTask st = saveTasks.get(config);
		if (st != null) {
			st.task.cancel();
			saveTasks.remove(config);
			st.saveAction.run();
			return true;
		}
		return false;
	}
}
