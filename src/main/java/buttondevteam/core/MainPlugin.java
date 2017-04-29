package buttondevteam.core;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.TBMCPlayerBase;
import net.milkbowl.vault.permission.Permission;

public class MainPlugin extends JavaPlugin {
	public static MainPlugin Instance;
	public static Permission permission;
	public static boolean Test;

	private PluginDescriptionFile pdfFile;
	private Logger logger;

	@Override
	public void onEnable() {
		// Logs "Plugin Enabled", registers commands
		Instance = this;
		pdfFile = getDescription();
		logger = getLogger();
		setupPermissions();
		Test = getConfig().getBoolean("test", true);
		saveConfig();
		TBMCChatAPI.AddCommand(this, UpdatePluginCommand.class);
		TBMCChatAPI.AddCommand(this, ScheduledRestartCommand.class);
		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerListener(), this);
		TBMCCoreAPI.RegisterUserClass(TBMCPlayerBase.class);
		logger.info(pdfFile.getName() + " has been Enabled (V." + pdfFile.getVersion() + ").");
	}

	@Override
	public void onDisable() {
		logger.info("Saving player data...");
		TBMCPlayerBase.savePlayers();
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
