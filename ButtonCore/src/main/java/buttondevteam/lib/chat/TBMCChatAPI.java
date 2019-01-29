package buttondevteam.lib.chat;

import buttondevteam.core.CommandCaller;
import buttondevteam.core.MainPlugin;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.core.component.channel.Channel.RecipientTestResult;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCChatPreprocessEvent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.architecture.Component;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

public class TBMCChatAPI {

    private static final HashMap<String, TBMCCommandBase> commands = new HashMap<>();

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
		for (Entry<String, TBMCCommandBase> cmd : TBMCChatAPI.GetCommands().entrySet()) {
			if (cmd.getKey().startsWith(command + " ")) {
				if (cmd.getValue().isPlayerOnly() && !(sender instanceof Player))
					continue;
                if (cmd.getValue().isModOnly() && (MainPlugin.permission != null ? !MainPlugin.permission.has(sender, "tbmc.admin") : !sender.isOp()))
					continue;
				int ind = cmd.getKey().indexOf(' ', command.length() + 2);
				if (ind >= 0) {
					String newcmd = cmd.getKey().substring(0, ind);
					if (!cmds.contains("/" + newcmd))
						addToCmds.accept("/" + newcmd);
				} else
					addToCmds.accept("/" + cmd.getKey());
			}
		}
        return cmds.toArray(new String[0]); //Apparently it's faster to use an empty array in modern Java
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
	public static synchronized void AddCommands(JavaPlugin plugin, Class<? extends TBMCCommandBase> acmdclass) {
		plugin.getLogger().info("Registering commands from " + acmdclass.getPackage().getName());
		Reflections rf = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage(acmdclass.getPackage().getName(),
						plugin.getClass().getClassLoader()))
				.addUrls(
						ClasspathHelper.forClass(OptionallyPlayerCommandBase.class,
								OptionallyPlayerCommandBase.class.getClassLoader()),
						ClasspathHelper.forClass(PlayerCommandBase.class, PlayerCommandBase.class.getClassLoader())) // http://stackoverflow.com/questions/12917417/using-reflections-for-finding-the-transitive-subtypes-of-a-class-when-not-all
				.addClassLoader(plugin.getClass().getClassLoader()).addScanners(new SubTypesScanner()));
		Set<Class<? extends TBMCCommandBase>> cmds = rf.getSubTypesOf(TBMCCommandBase.class);
		for (Class<? extends TBMCCommandBase> cmd : cmds) {
			try {
				if (!cmd.getPackage().getName().startsWith(acmdclass.getPackage().getName()))
					continue; // It keeps including the commands from here
				if (Modifier.isAbstract(cmd.getModifiers()))
					continue;
				TBMCCommandBase c = cmd.newInstance();
				c.plugin = plugin;
				CommandCaller.RegisterCommand(c); //Will check for nulls
				commands.put(c.GetCommandPath(), c);
			} catch (Exception e) {
				TBMCCoreAPI.SendException("An error occured while registering command " + cmd.getName(), e);
			}
		}
	}

	/**
	 * <p>
	 * This method adds a plugin's command to help and sets it's executor. They will be automatically unregistered on plugin disable.
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
                c = thecmdclass.getConstructor(Arrays.stream(params).map(Object::getClass).toArray(Class[]::new))
						.newInstance(params);
			else
				c = thecmdclass.newInstance();
			c.plugin = plugin;
			CommandCaller.RegisterCommand(c); //Will check for nulls
			commands.put(c.GetCommandPath(), c);
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while registering command " + thecmdclass.getSimpleName(), e);
		}
	}

	/**
	 * <p>
	 * This method adds a plugin's command to help and sets its executor. They will be automatically unregistered on plugin disable.
	 * </p>
	 * <p>
	 * The <u>command must be registered</u> in the caller plugin's plugin.yml. Otherwise the plugin will output a message to console.
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
		try {
			if (plugin == null) throw new IllegalArgumentException("The plugin is null!");
			if (cmd == null) throw new IllegalArgumentException("The command is null!");
			cmd.plugin = plugin;
			CommandCaller.RegisterCommand(cmd); //Checks for other nulls
			commands.put(cmd.GetCommandPath(), cmd);
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while registering command " + (cmd == null ? "n u l l" : cmd.GetCommandPath()), e);
		}
	}

	/**
	 * <p>
	 * This method adds a plugin's command to help and sets its executor. They will be automatically unregistered on component disable.
	 * </p>
	 * <p>
	 * The <u>command must be registered</u> in the caller plugin's plugin.yml. Otherwise the plugin will output a message to console.
	 * </p>
	 * <p>
	 * <i>Using this method after the server is done loading will have no effect.</i>
	 * </p>
	 *
	 * @param component The caller component
	 * @param cmd       The command to add
	 */
	public static void AddCommand(Component component, TBMCCommandBase cmd) {
		try {
			if (component == null) throw new IllegalArgumentException("The component is null!");
			if (cmd == null) throw new IllegalArgumentException("The command is null!");
			cmd.plugin = component.getPlugin();
			cmd.component = component;
			CommandCaller.RegisterCommand(cmd); //Checks for other nulls
			commands.put(cmd.GetCommandPath(), cmd);
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while registering command " + (cmd == null ? "n u l l" : cmd.GetCommandPath()), e);
		}
	}

	/**
	 * Removes all commands from the plugin
	 *
	 * @param plugin The plugin to remove commands from
	 */
	public static void RemoveCommands(JavaPlugin plugin) {
		commands.values().removeIf(c -> {
			try {
				if (c.plugin == plugin) {
					CommandCaller.UnregisterCommand(c);
					return true; //Remove
				}
				return false;
			} catch (Exception e) {
				TBMCCoreAPI.SendException("An error occured while unregistering commands for " + plugin.getName(), e);
				return true; //Remove if it couldn't get the plugin command
			}
		});
	}

	/**
	 * Removes all commands from the component
	 *
	 * @param component The component to remove commands from
	 */
	public static void RemoveCommands(Component component) {
		commands.values().removeIf(c -> {
			try {
				if (c.component == component) {
					CommandCaller.UnregisterCommand(c);
					return true; //Remove
				}
				return false;
			} catch (Exception e) {
				TBMCCoreAPI.SendException("An error occured while unregistering commands for " + component.getClass().getSimpleName(), e);
				return true; //Remove if it couldn't get the plugin command
			}
		});
	}

	/**
	 * Sends a chat message to Minecraft. Make sure that the channel is registered with {@link #RegisterChatChannel(Channel)}.<br>
	 *     This will also send the error message to the sender, if they can't send the message.
	 *
     * @param cm The message to send
	 * @return The event cancelled state
	 */
    public static boolean SendChatMessage(ChatMessage cm) {
	    return SendChatMessage(cm, cm.getUser().channel().get());
    }

	/**
	 * Sends a chat message to Minecraft. Make sure that the channel is registered with {@link #RegisterChatChannel(Channel)}.<br>
	 * This will also send the error message to the sender, if they can't send the message.
	 *
	 * @param cm      The message to send
	 * @param channel The MC channel to send in
	 * @return The event cancelled state
	 */
	public static boolean SendChatMessage(ChatMessage cm, Channel channel) {
		if (!Channel.getChannelList().contains(channel))
		    throw new RuntimeException("Channel " + channel.DisplayName().get() + " not registered!");
		if (!channel.Enabled().get()) {
			cm.getSender().sendMessage("§cThe channel '" + channel.DisplayName().get() + "' is disabled!");
			return true; //Cancel sending if channel is disabled
		}
		val permcheck = cm.getPermCheck();
	    RecipientTestResult rtr = getScoreOrSendError(channel, permcheck);
		int score = rtr.score;
		if (score == Channel.SCORE_SEND_NOPE || rtr.groupID == null)
			return true;
	    TBMCChatPreprocessEvent eventPre = new TBMCChatPreprocessEvent(cm.getSender(), channel, cm.getMessage());
		Bukkit.getPluginManager().callEvent(eventPre);
		if (eventPre.isCancelled())
			return true;
		cm.setMessage(eventPre.getMessage());
		TBMCChatEvent event;
		event = new TBMCChatEvent(channel, cm, rtr);
		Bukkit.getPluginManager().callEvent(event);
		return event.isCancelled();
	}

	/**
	 * Sends a regular message to Minecraft. Make sure that the channel is registered with {@link #RegisterChatChannel(Channel)}.
	 * 
	 * @param channel
	 *            The channel to send to
	 * @param rtr
	 *            The score&group to use to find the group - use {@link RecipientTestResult#ALL} if the channel doesn't have scores
	 * @param message
	 *            The message to send
	 * @param exceptions Platforms where this message shouldn't be sent (same as {@link ChatMessage#getOrigin()}
	 * @return The event cancelled state
	 */
	public static boolean SendSystemMessage(Channel channel, RecipientTestResult rtr, String message, String... exceptions) {
		if (!Channel.getChannelList().contains(channel))
			throw new RuntimeException("Channel " + channel.DisplayName().get() + " not registered!");
		if (!channel.Enabled().get())
			return true; //Cancel sending
		TBMCSystemChatEvent event = new TBMCSystemChatEvent(channel, message, rtr.score, rtr.groupID, exceptions);
		Bukkit.getPluginManager().callEvent(event);
		return event.isCancelled();
	}

	private static RecipientTestResult getScoreOrSendError(Channel channel, CommandSender sender) {
		RecipientTestResult result = channel.getRTR(sender);
		if (result.errormessage != null)
			sender.sendMessage("§c" + result.errormessage);
		return result;
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
