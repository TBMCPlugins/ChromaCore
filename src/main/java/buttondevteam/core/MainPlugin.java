package buttondevteam.core;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import buttondevteam.lib.EventExceptionCoreHandler;
import buttondevteam.lib.EventExceptionHandler;
import buttondevteam.lib.TBMCPlayer;

public class MainPlugin extends JavaPlugin {
	public static MainPlugin Instance;

	private PluginDescriptionFile pdfFile;
	private Logger logger;

	@Override
	public void onEnable() {
		// Logs "Plugin Enabled", registers commands
		Instance = this;
		pdfFile = getDescription();
		logger = getLogger();

		logger.info(pdfFile.getName() + " has been Enabled (V." + pdfFile.getVersion() + ").");
		EventExceptionHandler.registerEvents(new PlayerListener(), this, new EventExceptionCoreHandler());
	}

	@Override
	public void onDisable() {
		logger.info("Saving player data...");
		for (Entry<UUID, TBMCPlayer> entry : TBMCPlayer.getLoadedPlayers().entrySet()) {
			TBMCPlayer.savePlayer(entry.getValue());
		}
		logger.info("Player data saved.");
	}

}
