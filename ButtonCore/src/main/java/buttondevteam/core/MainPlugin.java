package buttondevteam.core;

import buttondevteam.lib.PluginUpdater;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.ChatRoom;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.TBMCPlayerBase;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.logging.Logger;

public class MainPlugin extends JavaPlugin {
	public static MainPlugin Instance;
    @Nullable
    public static Permission permission;
	public static boolean Test;
    public static Essentials ess;

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
		TBMCChatAPI.AddCommand(this, PrimeRestartCommand.class);
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
        try {
            Files.write(new File("plugins", "plugins.txt").toPath(), Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(p -> (CharSequence) p.getDataFolder().getName())::iterator);
        } catch (IOException e) {
            TBMCCoreAPI.SendException("Failed to write plugin list!", e);
        }
        ess = Essentials.getPlugin(Essentials.class);
		logger.info(pdfFile.getName() + " has been Enabled (V." + pdfFile.getVersion() + ") Test: " + Test + ".");
	}

	@Override
	public void onDisable() {
		logger.info("Saving player data...");
		TBMCPlayerBase.savePlayers();
		logger.info("Player data saved.");
        new Thread(() -> {
            File[] files = PluginUpdater.updatedir.listFiles();
            if (files == null)
                return;
            System.out.println("Updating " + files.length + " plugins...");
            for (File file : files) {
                try {
                    Files.move(file.toPath(), new File("plugins", file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Updated " + file.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Update complete!");
        }).start();
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
