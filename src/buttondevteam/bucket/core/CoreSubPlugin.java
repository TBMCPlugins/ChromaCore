package buttondevteam.bucket.core;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class CoreSubPlugin {
	public Plugin plugin;

	public CoreSubPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	public void register() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
		plugin.getLogger().log(Level.INFO, "Core subplugin registered!");
	}
}
