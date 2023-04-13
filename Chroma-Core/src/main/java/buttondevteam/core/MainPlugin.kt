package buttondevteam.core;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.core.component.channel.ChannelComponent;
import buttondevteam.core.component.channel.ChatRoom;
import buttondevteam.core.component.members.MemberComponent;
import buttondevteam.core.component.randomtp.RandomTPComponent;
import buttondevteam.core.component.restart.RestartComponent;
import buttondevteam.core.component.spawn.SpawnComponent;
import buttondevteam.core.component.towny.TownyComponent;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class MainPlugin extends ButtonPlugin {
	public static MainPlugin Instance;
	public static Permission permission;
	@Nullable
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

	/**
	 * Sets whether the plugin should write a list of installed plugins in a txt file.
	 * It can be useful if some other software needs to know the plugins.
	 */
	private final ConfigData<Boolean> writePluginList = getIConfig().getData("writePluginList", false);

	/**
	 * The chat format to use for messages from other platforms if Chroma-Chat is not installed.
	 */
	ConfigData<String> chatFormat = getIConfig().getData("chatFormat", "[{origin}|" +
		"{channel}] <{name}> {message}");

	/**
	 * Print some debug information.
	 */
	public final ConfigData<Boolean> test = getIConfig().getData("test", false);

	/**
	 * If a Chroma command clashes with another plugin's command, this setting determines whether the Chroma command should be executed or the other plugin's.
	 */
	public final ConfigData<Boolean> prioritizeCustomCommands = getIConfig().getData("prioritizeCustomCommands", false);

	@Override
	public void pluginEnable() {
		Instance = this;
		PluginDescriptionFile pdf = getDescription();
		logger = getLogger();
		if (!setupPermissions())
			throw new NullPointerException("No permission plugin found!");
		if (!setupEconomy()) //Though Essentials always provides economy, but we don't require Essentials
			getLogger().warning("No economy plugin found! Components using economy will not be registered.");
		saveConfig();
		Component.registerComponent(this, new RestartComponent());
		Component.registerComponent(this, new ChannelComponent());
		Component.registerComponent(this, new RandomTPComponent());
		Component.registerComponent(this, new MemberComponent());
		if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
			Component.registerComponent(this, new SpawnComponent());
		if (Bukkit.getPluginManager().isPluginEnabled("Towny")) //It fails to load the component class otherwise
			Component.registerComponent(this, new TownyComponent());
		/*if (Bukkit.getPluginManager().isPluginEnabled("Votifier") && economy != null)
			Component.registerComponent(this, new VotifierComponent(economy));*/
		ComponentManager.enableComponents();
		registerCommand(new ComponentCommand());
		registerCommand(new ChromaCommand());
		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerListener(), this);
		TBMCCoreAPI.RegisterEventsForExceptions(Companion.getCommand2MC(), this);
		ChromaGamerBase.addConverter(commandSender -> Optional.ofNullable(commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender
			? TBMCPlayer.getPlayer(new UUID(0, 0), TBMCPlayer.class) : null)); //Console & cmdblocks
		ChromaGamerBase.addConverter(sender -> Optional.ofNullable(sender instanceof Player
			? TBMCPlayer.getPlayer(((Player) sender).getUniqueId(), TBMCPlayer.class) : null)); //Players, has higher priority
		TBMCCoreAPI.RegisterUserClass(TBMCPlayerBase.class, TBMCPlayer::new);
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
		Supplier<Iterable<String>> playerSupplier = () -> Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName)::iterator;
		Companion.getCommand2MC().addParamConverter(OfflinePlayer.class, Bukkit::getOfflinePlayer, "Player not found!", playerSupplier);
		Companion.getCommand2MC().addParamConverter(Player.class, Bukkit::getPlayer, "Online player not found!", playerSupplier);
		if (writePluginList.get()) {
			try {
				Files.write(new File("plugins", "plugins.txt").toPath(), Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(p -> (CharSequence) p.getDataFolder().getName())::iterator);
			} catch (IOException e) {
				TBMCCoreAPI.SendException("Failed to write plugin list!", e, this);
			}
		}
		if (getServer().getPluginManager().isPluginEnabled("Essentials"))
			ess = Essentials.getPlugin(Essentials.class);
		logger.info(pdf.getName() + " has been Enabled (V." + pdf.getVersion() + ") Test: " + test.get() + ".");
	}

	@Override
	public void pluginDisable() {
		logger.info("Saving player data...");
		ChromaGamerBase.saveUsers();
		logger.info("Player data saved.");
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
