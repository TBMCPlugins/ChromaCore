package buttondevteam.bucket;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class MainPlugin extends JavaPlugin {
	public static MainPlugin Instance;

	private PluginDescriptionFile pdfFile;
	private Logger logger;

	public void onEnable() {
		// Logs "Plugin Enabled", registers commands
		Instance = this;
		pdfFile = getDescription();
		logger = getLogger();

		logger.info(pdfFile.getName() + " has been Enabled (V." + pdfFile.getVersion() + ").");
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
	}

}
