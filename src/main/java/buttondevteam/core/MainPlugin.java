package buttondevteam.core;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.TBMCPlayer;
import buttondevteam.lib.chat.TBMCChatAPI;
import net.milkbowl.vault.permission.Permission;

public class MainPlugin extends JavaPlugin {
	public static MainPlugin Instance;
	public static Permission permission;

	private PluginDescriptionFile pdfFile;
	private Logger logger;

	@Override
	public void onEnable() {
		// Logs "Plugin Enabled", registers commands
		Instance = this;
		pdfFile = getDescription();
		logger = getLogger();
		setupPermissions();
		TBMCChatAPI.AddCommand(this, UpdatePluginCommand.class);
		TBMCChatAPI.AddCommand(this, ScheduledRestartCommand.class);
		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerListener(), this);
		logger.info(pdfFile.getName() + " has been Enabled (V." + pdfFile.getVersion() + ").");
	}

	@Override
	public void onDisable() {
		logger.info("Saving player data...");
		for (Entry<UUID, TBMCPlayer> entry : TBMCPlayer.getLoadedPlayers().entrySet()) {
			TBMCPlayer.savePlayer(entry.getValue());
		}
		logger.info("Player data saved.");
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager()
				.getRegistration(Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}
}
