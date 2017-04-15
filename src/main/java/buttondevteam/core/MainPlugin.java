package buttondevteam.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.TBMCPlayerBase;
import net.milkbowl.vault.permission.Permission;

public class MainPlugin extends JavaPlugin {
	public static MainPlugin Instance;
	public static Permission permission;
	public static boolean Test;

	private PluginDescriptionFile pdfFile;
	private Logger logger;
	private int C = 0, keep = 0;

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
		Arrays.stream(new File(ChromaGamerBase.TBMC_PLAYERS_DIR).listFiles(f -> !f.isDirectory())).map(f -> {
			YamlConfiguration yc = new YamlConfiguration();
			try {
				yc.load(f);
			} catch (IOException | InvalidConfigurationException e) {
				TBMCCoreAPI.SendException("Error while converting player data!", e);
			}
			f.delete();
			return yc;
		}).forEach(yc -> {
			try {
				int flairtime = yc.getInt("flairtime"), fcount = yc.getInt("fcount"), fdeaths = yc.getInt("fdeaths");
				String flairstate = yc.getString("flairstate");
				List<String> usernames = yc.getStringList("usernames");
				boolean flaircheater = yc.getBoolean("flaircheater");
				final String uuid = yc.getString("uuid");
				C++;
				if ((fcount == 0 || fdeaths == 0)
						&& (flairstate == null || "NoComment".equals(flairstate) || flairtime <= 0))
					return; // Those who received no Fs yet will also get their stats reset if no flair
				final File file = new File(ChromaGamerBase.TBMC_PLAYERS_DIR + "minecraft", uuid + ".yml");
				YamlConfiguration targetyc = YamlConfiguration.loadConfiguration(file);
				targetyc.set("PlayerName", yc.getString("playername"));
				targetyc.set("minecraft_id", uuid);
				ConfigurationSection bc = targetyc.createSection("ButtonChat");
				bc.set("FlairTime", "NoComment".equals(flairstate) ? -3 : flairtime); // FlairTimeNone: -3
				bc.set("FCount", fcount);
				bc.set("FDeaths", fdeaths);
				bc.set("FlairState", flairstate);
				bc.set("UserNames", usernames);
				bc.set("FlairCheater", flaircheater);
				targetyc.save(file);
				keep++;
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Error while converting player data!", e);
			}
		});
		Bukkit.getScheduler().runTask(this, () -> logger.info("Converted " + keep + " player data from " + C));
		//TODO: Remove once ran it at least once
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
