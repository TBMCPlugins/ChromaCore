package buttondevteam.lib.architecture;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.ChromaUtils;
import lombok.*;
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
public class ConfigData<T> {
	private static final HashMap<Configuration, SaveTask> saveTasks = new HashMap<>();
	/**
	 * May be null for testing
	 */
	private ConfigurationSection config;
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private String path;
	protected final T def;
	private final Object primitiveDef;
	private final Runnable saveAction;
	/**
	 * The parameter is of a primitive type as returned by {@link YamlConfiguration#get(String)}
	 */
	private final Function<Object, T> getter;
	/**
	 * The result should be a primitive type or string that can be retrieved correctly later
	 */
	private final Function<T, Object> setter;

	/**
	 * The config value should not change outside this instance
	 */
	private T value;

	ConfigData(ConfigurationSection config, String path, T def, Object primitiveDef, Function<Object, T> getter, Function<T, Object> setter, Runnable saveAction) {
		if (def == null) {
			if (primitiveDef == null)
				throw new IllegalArgumentException("Either def or primitiveDef must be set.");
			if (getter == null)
				throw new IllegalArgumentException("A getter and setter must be present when using primitiveDef.");
			def = getter.apply(primitiveDef);
		} else if (primitiveDef == null)
			if (setter == null)
				primitiveDef = def;
			else
				primitiveDef = setter.apply(def);
		if ((getter == null) != (setter == null))
			throw new IllegalArgumentException("Both setters and getters must be present (or none if def is primitive).");
		this.config = config;
		this.path = path;
		this.def = def;
		this.primitiveDef = primitiveDef;
		this.getter = getter;
		this.setter = setter;
		this.saveAction = saveAction;
	}

	@Override
	public String toString() {
		return "ConfigData{" + "path='" + path + '\'' + ", value=" + value + '}';
	}

	void reset(ConfigurationSection config) {
		value = null;
		this.config = config;
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
		signalChange(config, saveAction);
	}

	static void signalChange(ConfigurationSection config, Runnable saveAction) {
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
		synchronized (saveTasks) {
			SaveTask st = saveTasks.get(config);
			if (st != null) {
				st.task.cancel();
				saveTasks.remove(config);
				st.saveAction.run();
				return true;
			}
		}
		return false;
	}

	public static <T> ConfigData.ConfigDataBuilder<T> builder(ConfigurationSection config, String path, Runnable saveAction) {
		return new ConfigDataBuilder<T>(config, path, saveAction);
	}

	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	public static class ConfigDataBuilder<T> {
		private final ConfigurationSection config;
		private final String path;
		private T def;
		private Object primitiveDef;
		private Function<Object, T> getter;
		private Function<T, Object> setter;
		private final Runnable saveAction;

		/**
		 * The default value to use, as used in code. If not a primitive type, use the {@link #getter(Function)} and {@link #setter(Function)} methods.
		 * <br/>
		 * To set the value as it is stored, use {@link #primitiveDef(Object)}.
		 *
		 * @param def The default value
		 * @return This builder
		 */
		public ConfigDataBuilder<T> def(T def) {
			this.def = def;
			return this;
		}

		/**
		 * The default value to use, as stored in yaml. Must be a primitive type. Make sure to use the {@link #getter(Function)} and {@link #setter(Function)} methods.
		 * <br/>
		 * To set the value as used in the code, use {@link #def(Object)}.
		 *
		 * @param primitiveDef The default value
		 * @return This builder
		 */
		public ConfigDataBuilder<T> primitiveDef(Object primitiveDef) {
			this.primitiveDef = primitiveDef;
			return this;
		}

		/**
		 * A function to use to obtain the runtime object from the yaml representation (usually string).
		 * The {@link #setter(Function)} must also be set.
		 *
		 * @param getter A function that receives the primitive type and returns the runtime type
		 * @return This builder
		 */
		public ConfigDataBuilder<T> getter(Function<Object, T> getter) {
			this.getter = getter;
			return this;
		}

		/**
		 * A function to use to obtain the yaml representation (usually string) from the runtime object.
		 * The {@link #getter(Function)} must also be set.
		 *
		 * @param setter A function that receives the runtime type and returns the primitive type
		 * @return This builder
		 */
		public ConfigDataBuilder<T> setter(Function<T, Object> setter) {
			this.setter = setter;
			return this;
		}

		/**
		 * Builds a modifiable config representation. Use if you want to change the value <i>in code</i>.
		 *
		 * @return A ConfigData instance.
		 */
		public ConfigData<T> build() {
			return new ConfigData<>(config, path, def, primitiveDef, getter, setter, saveAction);
		}

		/**
		 * Builds a read-only config representation. Use if you only want the value to be changed <i>in the config</i>.
		 *
		 * @return A ReadOnlyConfigData instance.
		 */
		public ReadOnlyConfigData<T> buildReadOnly() {
			return new ReadOnlyConfigData<>(config, path, def, primitiveDef, getter, setter, saveAction);
		}

		public String toString() {return "ConfigData.ConfigDataBuilder(config=" + this.config + ", path=" + this.path + ", def=" + this.def + ", primitiveDef=" + this.primitiveDef + ", getter=" + this.getter + ", setter=" + this.setter + ", saveAction=" + this.saveAction + ")";}
	}
}
