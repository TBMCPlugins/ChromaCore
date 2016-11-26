package buttondevteam.core;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import buttondevteam.lib.CPlayer;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.TBMCPlayer;
import buttondevteam.lib.db.CData;
import buttondevteam.lib.db.DataManager;

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

		/*setupDatabase();
		DataManager.setDatabase(getDatabase());
		final UUID cid = UUID.randomUUID();
		final UUID mcid = UUID.randomUUID();
		System.out.println(cid);
		System.out.println(mcid);
		System.out.println("----");
		DataManager.save(new CPlayer(cid, mcid));
		System.out.println("----");
		System.out.println(DataManager.load(CPlayer.class, cid).getMinecraftID());*/
		logger.info(pdfFile.getName() + " has been Enabled (V." + pdfFile.getVersion() + ").");
		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerListener(), this);
	}
	
    private void setupDatabase() {
        try {
            getDatabase().find(CPlayer.class).findRowCount();
        } catch (PersistenceException ex) {
            System.out.println("Installing database for ButtonCore due to first time usage");
            installDDL();
        }
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
