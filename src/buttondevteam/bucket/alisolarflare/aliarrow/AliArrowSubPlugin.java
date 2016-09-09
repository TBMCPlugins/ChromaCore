package buttondevteam.bucket.alisolarflare.aliarrow;

import java.util.logging.Level;

import buttondevteam.bucket.MainPlugin;

public class AliArrowSubPlugin {
	private MainPlugin plugin;

	public AliArrowSubPlugin(MainPlugin plugin){
		this.plugin = plugin;
	}
	public void register(){
		registerEvents();
		registerCommands();
		plugin.getLogger().log(Level.INFO, "Discord Sub Plugin Registered!");
	}
	private void registerEvents(){
		plugin.getServer().getPluginManager().registerEvents(new AliArrowListener(plugin), plugin);
		
	}
	private void registerCommands(){
		
	}
}
