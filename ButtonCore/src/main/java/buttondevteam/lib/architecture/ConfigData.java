package buttondevteam.lib.architecture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Use the getter/setter constructor if {@link T} isn't a primitive type or String.<br>
 *     Use {@link Component#getConfig()} or {@link ButtonPlugin#getIConfig()} then {@link IHaveConfig#getData(String, Object)} to get an instance.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
//@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ConfigData<T> { //TODO: Save after a while
	private final ConfigurationSection config;
	private final String path;
	private @Nullable final T def;
	private final Object primitiveDef;
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

	public ConfigData(ConfigurationSection config, String path, T def, Object primitiveDef, Function<Object, T> getter, Function<T, Object> setter) {
		this.config = config;
		this.path = path;
		this.def = def;
		this.primitiveDef = primitiveDef;
		this.getter = getter;
		this.setter = setter;
	}

	@SuppressWarnings("unchecked")
	public T get() {
		if (value != null) return value; //Speed things up
		Object val = config.get(path);
		if (val == null) {
			val = primitiveDef;
		}
		if (val == primitiveDef && !saved) {
			if (def != null)
				set(def); //Save default value
			else
				config.set(path, primitiveDef);
			saved = true;
		}
		if (getter != null) {
			T hmm = getter.apply(val);
			if (hmm == null) hmm = def; //Set if the getter returned null
			return hmm;
		}
		return (T) val;
	}

	public void set(T value) {
		Object val;
		if (setter != null)
			val = setter.apply(value);
		else val = value;
		config.set(path, val);
		this.value =value;
	}
}
