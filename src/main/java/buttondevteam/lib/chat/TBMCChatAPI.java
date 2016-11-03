package buttondevteam.lib.chat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.TBMCPlayer;

public class TBMCChatAPI {

	private static HashMap<String, TBMCCommandBase> commands = new HashMap<String, TBMCCommandBase>();

	public static HashMap<String, TBMCCommandBase> GetCommands() {
		return commands;
	}

	public static String[] GetSubCommands(TBMCCommandBase command) {
		ArrayList<String> cmds = new ArrayList<String>();
		cmds.add("§6---- Subcommands ----");
		for (TBMCCommandBase cmd : TBMCChatAPI.GetCommands().values()) {
			if (cmd.GetCommandPath().startsWith(command.GetCommandPath() + "/")) {
				int ind = cmd.GetCommandPath().indexOf('/', command.GetCommandPath().length() + 2);
				if (ind >= 0)
					continue;
				cmds.add(cmd.GetCommandPath().replace('/', ' '));
			}
		}
		return cmds.toArray(new String[cmds.size()]);
	}

	/**
	 * <p>
	 * This method adds a plugin's commands to help and sets their executor.
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
				commands.put(c.GetCommandPath(), c);
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
		plugin.getLogger().info("Registering command " + thecmdclass.getName() + " for " + plugin.getName());
		try {
			TBMCCommandBase c = thecmdclass
					.getConstructor(Arrays.stream(params).map(p -> p.getClass()).toArray(Class[]::new))
					.newInstance(params);
			c.plugin = plugin;
			commands.put(c.GetCommandPath(), c);
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while registering command " + thecmdclass.getName(), e);
		}
	}

	/**
	 * <p>
	 * Add player information for {@link PlayerInfoCommand}. Only mods can see the given information.
	 * </p>
	 * 
	 * @param player
	 * @param infoline
	 */
	public void AddPlayerInfoForMods(TBMCPlayer player, String infoline) {
		// TODO
	}

	/**
	 * <p>
	 * Add player information for hover text at {@link ChatProcessing}. Every online player can see the given information.
	 * </p>
	 * 
	 * @param player
	 * @param infoline
	 */
	public void AddPlayerInfoForHover(TBMCPlayer player, String infoline) {
		// TODO
	}
}
