package buttondevteam.core.player;

import org.bukkit.plugin.Plugin;

public class CoreSubPlugin {
	public Plugin plugin;

	public CoreSubPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	public void register() {
		System.out.println("Players subplugin registered!");
	}
}
