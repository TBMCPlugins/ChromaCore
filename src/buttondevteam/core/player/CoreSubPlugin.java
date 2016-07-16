package buttondevteam.core.player;

import org.bukkit.plugin.Plugin;

public class PlayerSubPlugin {
	public static Plugin plugin;

	public PlayerSubPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	public void register() {
		System.out.println("Players subplugin registered!");
	}
}
