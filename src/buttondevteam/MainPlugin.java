package buttondevteam;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import buttondevteam.alisolarflare.aliarrow.AliArrowSubPlugin;

public class MainPlugin extends JavaPlugin {
	private PluginDescriptionFile pdfFile;
	private Logger logger;
	private AliArrowSubPlugin aliArrowSubPlugin;

	public void onEnable(){
		//Logs "Plugin Enabled", registers commands
		pdfFile = getDescription();
		logger = getLogger();
		logger.info(pdfFile.getName() + " has been started (V." + pdfFile.getVersion()+ ").");

		registerSubPlugins();
		registerCommands();
		registerEvents();

		logger.info(pdfFile.getName() + " has been Enabled (V." + pdfFile.getVersion()+ ").");
	}
	private void registerSubPlugins() {
		aliArrowSubPlugin = new AliArrowSubPlugin(this);
		aliArrowSubPlugin.register();
	}
	private void registerCommands() {
		// TODO Auto-generated method stub
		
	}
	private void registerEvents() {
		// TODO Auto-generated method stub
		
	}
	
}
