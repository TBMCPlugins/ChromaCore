package buttondevteam.lib.player;

import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerData<T> {
	private String name;
	private YamlConfiguration yaml;
	private T def;

	public PlayerData(String name, YamlConfiguration yaml, T def) {
		this.name = name;
		this.yaml = yaml;
		this.def = def;
	}

	@SuppressWarnings("unchecked")
	// @Deprecated - What was once enforced (2 days ago from now) vanished now
	public T get() {
		Object value = yaml.get(name, def);
		if (value instanceof Integer) {
			if (def instanceof Short) // If the default is Short the value must be as well because both are T
				return (T) (Short) ((Integer) value).shortValue();
			if (def instanceof Long)
				return (T) (Long) ((Integer) value).longValue();
		}
		return (T) value;
	}

	public void set(T value) {
		yaml.set(name, value);
	}

	@Override
	public String toString() {
		return get().toString();
	}
}
