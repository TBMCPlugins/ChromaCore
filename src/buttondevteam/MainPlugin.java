package buttondevteam;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import buttondevteam.alisolarflare.aliarrow.AliArrowSubPlugin;
import buttondevteam.core.CoreSubPlugin;

public class MainPlugin extends JavaPlugin {
	private PluginDescriptionFile pdfFile;
	private Logger logger;
	private AliArrowSubPlugin aliArrowSubPlugin;
	private CoreSubPlugin playerSubPlugin;

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
		playerSubPlugin = new CoreSubPlugin(this);
		playerSubPlugin.register();
	}
	private void registerCommands() {
		// TODO Auto-generated method stub
		
	}
	private void registerEvents() {
		// TODO Auto-generated method stub
		
	}
	
}
