package buttondevteam.lib.player;

import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerData<T> {
	private String name;
	private YamlConfiguration yaml;

	public PlayerData(String name, YamlConfiguration yaml) {
		this.name = name;
		this.yaml = yaml;
	}

	@SuppressWarnings("unchecked")
	public T get() {
		Object value = yaml.get(name);
		return (T) value;
	}

	public void set(T value) {
		yaml.set(name, value);
	}

	public T getOrDefault(T def) {
		T value = get();
		return value == null ? def : value;
	}

	@Override
	public String toString() {
		return get().toString();
	}
}
