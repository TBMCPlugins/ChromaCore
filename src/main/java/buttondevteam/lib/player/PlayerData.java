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
		return (T) yaml.get(name);
	}

	public void set(String value) {
		yaml.set(name, value);
	}
}
