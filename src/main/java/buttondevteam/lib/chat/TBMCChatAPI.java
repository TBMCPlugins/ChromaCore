package buttondevteam.lib.chat;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import buttondevteam.core.CommandCaller;
import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Channel.RecipientTestResult;

public class TBMCChatAPI {

	private static HashMap<String, TBMCCommandBase> commands = new HashMap<String, TBMCCommandBase>();

	public static HashMap<String, TBMCCommandBase> GetCommands() {
		return commands;
	}

	/**
	 * Returns messages formatted for Minecraft chat listing the subcommands of the command.
	 * 
	 * @param command
	 *            The command which we want the subcommands of
	 * @param sender
	 *            The sender for permissions
	 * @return The subcommands
	 */
	public static String[] GetSubCommands(TBMCCommandBase command, CommandSender sender) {
		return GetSubCommands(command.GetCommandPath(), sender);
	}

	/**
	 * Returns messages formatted for Minecraft chat listing the subcommands of the command.<br>
	 * Returns a header if subcommands were found, otherwise returns an empty array.
	 * 
	 * @param command
	 *            The command which we want the subcommands of
	 * @param sender
	 *            The sender for permissions
	 * @return The subcommands
	 */
	public static String[] GetSubCommands(String command, CommandSender sender) {
		ArrayList<String> cmds = new ArrayList<String>();
		Consumer<String> addToCmds = cmd -> {
			if (cmds.size() == 0)
				cmds.add("§6---- Subcommands ----");
			cmds.add(cmd);
		};
		for (TBMCCommandBase cmd : TBMCChatAPI.GetCommands().values()) {
			if (cmd.GetCommandPath().startsWith(command + " ")) {
				if (cmd.isPlayerOnly() && !(sender instanceof Player))
					continue;
				if (cmd.getClass().getAnnotation(CommandClass.class).modOnly()
						&& !MainPlugin.permission.has(sender, "tbmc.admin"))
					continue;
				int ind = cmd.GetCommandPath().indexOf(' ', command.length() + 2);
				if (ind >= 0) {
					String newcmd = cmd.GetCommandPath().substring(0, ind);
					if (!cmds.contains("/" + newcmd))
						addToCmds.accept("/" + newcmd);
				} else
					addToCmds.accept("/" + cmd.GetCommandPath());
			}
		}
		return cmds.toArray(new String[cmds.size()]);
	}

	/**
	 * <p>
	 * This method adds a plugin's commands to help and sets their executor.
	 * </p>
	 * <p>
	 * </p>
	 * <b>The command classes have to have a constructor each with no parameters</b>
	 * <p>
	 * The <u>command must be registered</u> in the caller plugin's plugin.yml. Otherwise the plugin will output a messsage to console.
	 * </p>
	 * <p>
	 * <i>Using this method after the server is done loading will have no effect.</i>
	 * </p>
	 * 
	 * @param plugin
	 *            The caller plugin
	 * @param acmdclass
	 *            A command's class to get the package name for commands. The provided class's package and subpackages are scanned for commands.
	 */
	public static void AddCommands(JavaPlugin plugin, Class<? extends TBMCCommandBase> acmdclass) {
		plugin.getLogger().info("Registering commands for " + plugin.getName());
		Reflections rf = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage(acmdclass.getPackage().getName(),
						plugin.getClass().getClassLoader()))
				.addClassLoader(plugin.getClass().getClassLoader()).addScanners(new SubTypesScanner()));
		Set<Class<? extends TBMCCommandBase>> cmds = rf.getSubTypesOf(TBMCCommandBase.class);
		for (Class<? extends TBMCCommandBase> cmd : cmds) {
			try {
				if (Modifier.isAbstract(cmd.getModifiers()))
					continue;
				TBMCCommandBase c = cmd.newInstance();
				c.plugin = plugin;
				if (!CheckForNulls(plugin, c))
					continue;
				commands.put(c.GetCommandPath(), c);
				CommandCaller.RegisterCommand(c);
			} catch (InstantiationException e) {
				TBMCCoreAPI.SendException("An error occured while registering command " + cmd.getName(), e);
			} catch (IllegalAccessException e) {
				TBMCCoreAPI.SendException("An error occured while registering command " + cmd.getName(), e);
			}
		}
	}

	/**
	 * <p>
	 * This method adds a plugin's command to help and sets it's executor.
	 * </p>
	 * <p>
	 * The <u>command must be registered</u> in the caller plugin's plugin.yml. Otherwise the plugin will output a messsage to console.
	 * </p>
	 * <p>
	 * <i>Using this method after the server is done loading will have no effect.</i>
	 * </p>
	 * 
	 * @param plugin
	 *            The caller plugin
	 * @param thecmdclass
	 *            The command's class to create it (because why let you create the command class)
	 */
	public static void AddCommand(JavaPlugin plugin, Class<? extends TBMCCommandBase> thecmdclass, Object... params) {
		// plugin.getLogger().info("Registering command " + thecmdclass.getSimpleName() + " for " + plugin.getName());
		try {
			TBMCCommandBase c;
			if (params.length > 0)
				c = thecmdclass.getConstructor(Arrays.stream(params).map(p -> p.getClass()).toArray(Class[]::new))
						.newInstance(params);
			else
				c = thecmdclass.newInstance();
			c.plugin = plugin;
			if (!CheckForNulls(plugin, c))
				return;
			commands.put(c.GetCommandPath(), c);
			CommandCaller.RegisterCommand(c);
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while registering command " + thecmdclass.getSimpleName(), e);
		}
	}

	/**
	 * <p>
	 * This method adds a plugin's command to help and sets it's executor.
	 * </p>
	 * <p>
	 * The <u>command must be registered</u> in the caller plugin's plugin.yml. Otherwise the plugin will output a messsage to console.
	 * </p>
	 * <p>
	 * <i>Using this method after the server is done loading will have no effect.</i>
	 * </p>
	 * 
	 * @param plugin
	 *            The caller plugin
	 * @param cmd
	 *            The command to add
	 */
	public static void AddCommand(JavaPlugin plugin, TBMCCommandBase cmd) {
		if (!CheckForNulls(plugin, cmd))
			return;
		// plugin.getLogger().info("Registering command /" + cmd.GetCommandPath() + " for " + plugin.getName());
		try {
			cmd.plugin = plugin;
			commands.put(cmd.GetCommandPath(), cmd);
			CommandCaller.RegisterCommand(cmd);
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while registering command " + cmd.GetCommandPath(), e);
		}
	}

	private static boolean CheckForNulls(JavaPlugin plugin, TBMCCommandBase cmd) {
		if (cmd == null) {
			TBMCCoreAPI.SendException("An error occured while registering a command for plugin " + plugin.getName(),
					new Exception("The command is null!"));
			return false;
		} else if (cmd.GetCommandPath() == null) {
			TBMCCoreAPI.SendException("An error occured while registering command " + cmd.getClass().getSimpleName()
					+ " for plugin " + plugin.getName(), new Exception("The command path is null!"));
			return false;
		}
		return true;
	}

	/**
	 * Sends a chat message to Minecraft. Make sure that the channel is registered with {@link #RegisterChatChannel(Channel)}.
	 * 
	 * @param channel
	 *            The channel to send to
	 * @param sender
	 *            The sender to send from
	 * @param message
	 *            The message to send
	 * @return The event cancelled state
	 */
	public static boolean SendChatMessage(Channel channel, CommandSender sender, String message) {
		if (!Channel.getChannels().contains(channel))
			throw new RuntimeException("Channel " + channel.DisplayName + " not registered!");
		int score;
		if (channel.filteranderrormsg == null)
			score = -1;
		else {
			RecipientTestResult result = channel.filteranderrormsg.apply(sender);
			if (result.errormessage != null) {
				sender.sendMessage("§c" + result.errormessage);
				return true;
			}
			score = result.score;
		}
		TBMCChatEvent event = new TBMCChatEvent(sender, channel, message, score);
		Bukkit.getPluginManager().callEvent(event);
		return event.isCancelled();
	}

	/**
	 * Register a chat channel. See {@link Channel#Channel(String, Color, String, java.util.function.Function)} for details.
	 * 
	 * @param channel
	 *            A new {@link Channel} to register
	 */
	public static void RegisterChatChannel(Channel channel) {
		Channel.RegisterChannel(channel);
	}
}
