package buttondevteam.lib.architecture;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.function.Function;

/**
 * Use the getter/setter constructor if {@link T} isn't a primitive type or String.
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class ConfigData<T> { //TODO: Save after a while
	private final ConfigurationSection config;
	private final String path;
	private final T def;
	/**
	 * The parameter is of a primitive type as returned by {@link YamlConfiguration#get(String)}
	 */
	private Function<Object, T> getter;
	/**
	 * The result should be a primitive type or string that can be retrieved correctly later
	 */
	private Function<T, Object> setter;

	@SuppressWarnings("unchecked")
	public T get() {
		Object val = config.get(path, def);
		if (getter != null)
			return getter.apply(val);
		return (T) val;
	}

	public void set(T value) {
		Object val;
		if (setter != null)
			val = setter.apply(value);
		else val = value;
		config.set(path, val);
	}
}
