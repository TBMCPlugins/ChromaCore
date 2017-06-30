package buttondevteam.core;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.Channel.RecipientTestResult;
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
		TBMCChatAPI.RegisterChatChannel(Channel.GlobalChat = new Channel("§fg§f", Color.White, "g", null));
		TBMCChatAPI.RegisterChatChannel(Channel.AdminChat = new Channel("§cADMIN§f", Color.Red, "a",
				s -> s.isOp() //
						? new RecipientTestResult(0)
						: new RecipientTestResult("You need to be an admin to use this channel.")));
		TBMCChatAPI.RegisterChatChannel(Channel.ModChat = new Channel("§9MOD§f", Color.Blue, "mod",
				s -> s.isOp() || (s instanceof Player && MainPlugin.permission.playerInGroup((Player) s, "mod"))
						? new RecipientTestResult(0) //
						: new RecipientTestResult("You need to be a mod to use this channel.")));
		logger.info(pdfFile.getName() + " has been Enabled (V." + pdfFile.getVersion() + ") Test: " + Test + ".");
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
