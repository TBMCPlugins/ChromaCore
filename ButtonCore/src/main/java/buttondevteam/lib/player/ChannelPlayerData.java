package buttondevteam.lib.player;

import buttondevteam.component.channel.Channel;
import org.bukkit.configuration.file.YamlConfiguration;

public class ChannelPlayerData { //I just want this to work
	private final PlayerData<String> data;
	private final Channel def;

	public ChannelPlayerData(String name, YamlConfiguration yaml, Channel def) {
		data = new PlayerData<>(name, yaml, "");
		this.def = def;
	}

	public Channel get() {
		String str = data.get();
		if (str.isEmpty())
			return def;
		return Channel.getChannels().filter(c -> str.equals(c.ID)).findAny().orElse(def);
	}

	public void set(Channel value) {
		data.set(value.ID);
	}
}
