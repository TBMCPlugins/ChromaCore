package buttondevteam.core;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.ChatRoom;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.TBMCPlayerBase;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

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
		TBMCChatAPI.RegisterChatChannel(Channel.GlobalChat = new Channel("§fOOC§f", Color.White, "ooc", null));
		Channel.GlobalChat.IDs = new String[]{"g"}; //Support /g as well
		TBMCChatAPI.RegisterChatChannel(
				Channel.AdminChat = new Channel("§cADMIN§f", Color.Red, "a", Channel.inGroupFilter(null)));
		TBMCChatAPI.RegisterChatChannel(
				Channel.ModChat = new Channel("§9MOD§f", Color.Blue, "mod", Channel.inGroupFilter("mod")));
		TBMCChatAPI.RegisterChatChannel(new Channel("§6DEV§", Color.Gold, "dev", Channel.inGroupFilter("developer")));
		TBMCChatAPI.RegisterChatChannel(new ChatRoom("§cRED", Color.DarkRed, "red"));
		TBMCChatAPI.RegisterChatChannel(new ChatRoom("§6ORANGE", Color.Gold, "orange"));
		TBMCChatAPI.RegisterChatChannel(new ChatRoom("§eYELLOW", Color.Yellow, "yellow"));
		TBMCChatAPI.RegisterChatChannel(new ChatRoom("§aGREEN", Color.Green, "green"));
		TBMCChatAPI.RegisterChatChannel(new ChatRoom("§bBLUE", Color.Blue, "blue"));
		TBMCChatAPI.RegisterChatChannel(new ChatRoom("§5PURPLE", Color.DarkPurple, "purple"));
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
