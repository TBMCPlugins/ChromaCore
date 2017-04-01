package buttondevteam.lib.player;

import org.bukkit.configuration.file.YamlConfiguration;

public class EnumPlayerData<T extends Enum<T>> {
	private PlayerData<String> data;
	private Class<T> cl;

	public EnumPlayerData(String name, YamlConfiguration yaml, Class<T> cl) {
		data = new PlayerData<String>(name, yaml);
		this.cl = cl;
	}

	public T get() {
		return Enum.valueOf(cl, data.get());
	}

	public void set(T value) {
		data.set(value.toString());
	}

	public T getOrDefault(T def) {
		T value = get();
		return value == null ? def : value;
	}
}
