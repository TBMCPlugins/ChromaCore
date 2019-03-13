package buttondevteam.core;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.core.component.channel.ChannelComponent;
import buttondevteam.core.component.channel.ChatRoom;
import buttondevteam.core.component.members.MemberComponent;
import buttondevteam.core.component.randomtp.RandomTPComponent;
import buttondevteam.core.component.restart.RestartComponent;
import buttondevteam.core.component.towny.TownyComponent;
import buttondevteam.core.component.updater.PluginUpdater;
import buttondevteam.core.component.updater.PluginUpdaterComponent;
import buttondevteam.core.component.votifier.VotifierComponent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerBase;
import com.earth2me.essentials.Essentials;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class MainPlugin extends ButtonPlugin {
	public static MainPlugin Instance;
    public static Permission permission;
	public static boolean Test;
    public static Essentials ess;

	private Logger logger;
	@Nullable
	private Economy economy;
	/**
	 * Whether the Core's chat handler should be enabled.
	 * Other chat plugins handling messages from other platforms should set this to false.
	 */
	@Getter
	@Setter
	private boolean chatHandlerEnabled = true;

	private ConfigData<Boolean> writePluginList() {
		return getIConfig().getData("writePluginList", false);
	}

	ConfigData<String> chatFormat() {
		return getIConfig().getData("chatFormat", "[{origin}|" +
			"{channel}] <{name}> {message}");
	}

	@Override
	public void pluginEnable() {
		// Logs "Plugin Enabled", registers commands
		Instance = this;
        PluginDescriptionFile pdf = getDescription();
		logger = getLogger();
		if (!setupPermissions())
			throw new NullPointerException("No permission plugin found!");
		if (!setupEconomy()) //Though Essentials always provides economy so this shouldn't happen
			getLogger().warning("No economy plugin found! Components using economy will not be registered.");
		Test = getConfig().getBoolean("test", true);
		saveConfig();
		Component.registerComponent(this, new PluginUpdaterComponent());
		Component.registerComponent(this, new RestartComponent());
		//noinspection unchecked - needed for testing
		Component.registerComponent(this, new ChannelComponent());
		Component.registerComponent(this, new RandomTPComponent());
		Component.registerComponent(this, new MemberComponent());
		if (Bukkit.getPluginManager().isPluginEnabled("Towny")) //It fails to load the component class otherwise
			Component.registerComponent(this, new TownyComponent());
		if (Bukkit.getPluginManager().isPluginEnabled("Votifier") && economy != null)
			Component.registerComponent(this, new VotifierComponent(economy));
		ComponentManager.enableComponents();
		getCommand2MC().registerCommand(new ComponentCommand());
		getCommand2MC().registerCommand(new ThorpeCommand());
		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerListener(), this);
		ChromaGamerBase.addConverter(commandSender -> Optional.ofNullable(commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender
				? TBMCPlayer.getPlayer(new UUID(0, 0), TBMCPlayer.class) : null)); //Console & cmdblocks
		ChromaGamerBase.addConverter(sender -> Optional.ofNullable(sender instanceof Player
				? TBMCPlayer.getPlayer(((Player) sender).getUniqueId(), TBMCPlayer.class) : null)); //Players, has higher priority
		TBMCCoreAPI.RegisterUserClass(TBMCPlayerBase.class);
		TBMCChatAPI.RegisterChatChannel(Channel.GlobalChat = new Channel("§fg§f", Color.White, "g", null)); //The /ooc ID has moved to the config
		TBMCChatAPI.RegisterChatChannel(
				Channel.AdminChat = new Channel("§cADMIN§f", Color.Red, "a", Channel.inGroupFilter(null)));
		TBMCChatAPI.RegisterChatChannel(
				Channel.ModChat = new Channel("§9MOD§f", Color.Blue, "mod", Channel.inGroupFilter("mod")));
        TBMCChatAPI.RegisterChatChannel(new Channel("§6DEV§f", Color.Gold, "dev", Channel.inGroupFilter("developer")));
        TBMCChatAPI.RegisterChatChannel(new ChatRoom("§cRED§f", Color.DarkRed, "red"));
        TBMCChatAPI.RegisterChatChannel(new ChatRoom("§6ORANGE§f", Color.Gold, "orange"));
        TBMCChatAPI.RegisterChatChannel(new ChatRoom("§eYELLOW§f", Color.Yellow, "yellow"));
        TBMCChatAPI.RegisterChatChannel(new ChatRoom("§aGREEN§f", Color.Green, "green"));
        TBMCChatAPI.RegisterChatChannel(new ChatRoom("§bBLUE§f", Color.Blue, "blue"));
        TBMCChatAPI.RegisterChatChannel(new ChatRoom("§5PURPLE§f", Color.DarkPurple, "purple"));
		if (writePluginList().get()) {
			try {
				Files.write(new File("plugins", "plugins.txt").toPath(), Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(p -> (CharSequence) p.getDataFolder().getName())::iterator);
			} catch (IOException e) {
				TBMCCoreAPI.SendException("Failed to write plugin list!", e);
			}
        }
        ess = Essentials.getPlugin(Essentials.class);
        logger.info(pdf.getName() + " has been Enabled (V." + pdf.getVersion() + ") Test: " + Test + ".");
	}

	@Override
	public void pluginDisable() {
		logger.info("Saving player data...");
		TBMCPlayerBase.savePlayers();
		logger.info("Player data saved.");
        new Thread(() -> {
            File[] files = PluginUpdater.updatedir.listFiles();
            if (files == null)
                return;
	        logger.info("Updating " + files.length + " plugins...");
            for (File file : files) {
                try {
                    Files.move(file.toPath(), new File("plugins", file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
	                logger.info("Updated " + file.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
	        logger.info("Update complete!");
        }).start();
	}

	private boolean setupPermissions() {
		permission = setupProvider(Permission.class);
		return (permission != null);
	}

	private boolean setupEconomy() {
		economy = setupProvider(Economy.class);
		return (economy != null);
	}

	private <T> T setupProvider(Class<T> cl) {
		RegisteredServiceProvider<T> provider = getServer().getServicesManager()
			.getRegistration(cl);
		if (provider != null)
			return provider.getProvider();
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equals("dontrunthiscmd")) return true; //Used in chat preprocess for console
		sender.sendMessage("§cThis command isn't available."); //In theory, unregistered commands use this method
		return true;
	}
}
