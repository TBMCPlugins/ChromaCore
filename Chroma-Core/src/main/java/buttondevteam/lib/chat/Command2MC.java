package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.architecture.Component;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import lombok.val;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Command2MC extends Command2<ICommand2MC, Command2MCSender> implements Listener {
	/**
	 * Don't use directly, use the method in Component and ButtonPlugin to automatically unregister the command when needed.
	 *
	 * @param command The command to register
	 */
	@Override
	public void registerCommand(ICommand2MC command) {
		var subcmds = super.registerCommand(command, '/');
		var perm = "chroma.command." + command.getCommandPath().replace(' ', '.');
		if (Bukkit.getPluginManager().getPermission(perm) == null) //Check needed for plugin reset
			Bukkit.getPluginManager().addPermission(new Permission(perm,
				PermissionDefault.TRUE)); //Allow commands by default, it will check mod-only
		for (val method : command.getClass().getMethods()) {
			if (!method.isAnnotationPresent(Subcommand.class)) continue;
			var path = getCommandPath(method.getName(), '.');
			if (path.length() > 0) {
				var subperm = perm + path;
				if (Bukkit.getPluginManager().getPermission(subperm) == null) //Check needed for plugin reset
					Bukkit.getPluginManager().addPermission(new Permission(subperm,
						PermissionDefault.TRUE)); //Allow commands by default, it will check mod-only
			}
			String pg = permGroup(command, method);
			if (pg.length() == 0) continue;
			perm = "chroma." + pg;
			if (Bukkit.getPluginManager().getPermission(perm) == null) //It may occur multiple times
				Bukkit.getPluginManager().addPermission(new Permission(perm,
					PermissionDefault.OP)); //Do not allow any commands that belong to a group
		}

		registerOfficially(command, subcmds);
	}

	@Override
	public boolean hasPermission(Command2MCSender sender, ICommand2MC command, Method method) {
		return hasPermission(sender.getSender(), command, method);
	}

	public boolean hasPermission(CommandSender sender, ICommand2MC command, Method method) {
		if (sender instanceof ConsoleCommandSender) return true; //Always allow the console
		if (command == null) return true; //Allow viewing the command - it doesn't do anything anyway
		String pg;
		boolean p = true;
		var cmdperm = "chroma.command." + command.getCommandPath().replace(' ', '.');
		var path = getCommandPath(method.getName(), '.');
		String[] perms = {
			path.length() > 0 ? cmdperm + path : null,
			cmdperm,
			(pg = permGroup(command, method)).length() > 0 ? "chroma." + pg : null
		};
		for (String perm : perms) {
			if (perm != null) {
				if (p) { //Use OfflinePlayer to avoid fetching player data
					if (sender instanceof OfflinePlayer)
						p = MainPlugin.permission.playerHas(sender instanceof Player ? ((Player) sender).getLocation().getWorld().getName() : null, (OfflinePlayer) sender, perm);
					else
						p = false; //Use sender's method
					if (!p) p = sender.hasPermission(perm);
				} else break; //If any of the permissions aren't granted then don't allow
			}
		}
		return p;
	}

	/**
	 * Returns the first group found in the hierarchy starting from the command method <b>or</b> the mod group if <i>any</i></i> of the superclasses are mod only.
	 *
	 * @param method The subcommand to check
	 * @return The permission group for the subcommand or empty string
	 */
	private String permGroup(ICommand2MC command, Method method) {
		if (method != null) {
			val sc = method.getAnnotation(Subcommand.class);
			if (sc != null && sc.permGroup().length() > 0) {
				return sc.permGroup();
			}
		}
		if (getAnnForValue(command.getClass(), CommandClass.class, CommandClass::modOnly, false))
			return Subcommand.MOD_GROUP;
		return getAnnForValue(command.getClass(), CommandClass.class, CommandClass::permGroup, "");
	}

	/**
	 * Loops until it finds a value that is <b>not</b> the same as def
	 *
	 * @param sourceCl  The class which has the annotation
	 * @param annCl     The annotation to get
	 * @param annMethod The annotation method to check
	 * @param def       The value to ignore when looking for the result
	 * @param <T>       The annotation type
	 * @param <V>       The type of the value
	 * @return The value returned by the first superclass or def
	 */
	private <T extends Annotation, V> V getAnnForValue(Class<?> sourceCl, Class<T> annCl, Function<T, V> annMethod, V def) {
		for (Class<?> cl = sourceCl; cl != null; cl = cl.getSuperclass()) {
			val cc = cl.getAnnotation(annCl);
			V r;
			if (cc != null && (r = annMethod.apply(cc)) != def) return r;
		}
		return def;
	}

	/**
	 * Automatically colors the message red.
	 * {@see super#addParamConverter}
	 */
	@Override
	public <T> void addParamConverter(Class<T> cl, Function<String, T> converter, String errormsg) {
		super.addParamConverter(cl, converter, "§c" + errormsg);
	}

	public void unregisterCommands(ButtonPlugin plugin) {
		/*var cmds = subcommands.values().stream().map(sd -> sd.command).filter(cmd -> plugin.equals(cmd.getPlugin())).toArray(ICommand2MC[]::new);
		for (var cmd : cmds)
			unregisterCommand(cmd);*/
		subcommands.values().removeIf(sd -> Optional.ofNullable(sd.command).map(ICommand2MC::getPlugin).map(plugin::equals).orElse(false));
	}

	public void unregisterCommands(Component<?> component) {
		/*var cmds = subcommands.values().stream().map(sd -> sd.command).filter(cmd -> component.equals(cmd.getComponent())).toArray(ICommand2MC[]::new);
		for (var cmd : cmds)
			unregisterCommand(cmd);*/
		subcommands.values().removeIf(sd -> Optional.ofNullable(sd.command).map(ICommand2MC::getComponent)
			.map(comp -> component.getClass().getSimpleName().equals(comp.getClass().getSimpleName())).orElse(false));
	}

	@EventHandler
	private void handleTabComplete(TabCompleteEvent event) {
		String commandline = event.getBuffer();
		CommandSender sender = event.getSender();
		for (int i = commandline.length(); i != -1; i = commandline.lastIndexOf(' ', i - 1)) {
			String subcommand = commandline.substring(0, i).toLowerCase();
			if (subcommand.length() == 0 || subcommand.charAt(0) != '/') subcommand = '/' + subcommand; //Console
			SubcommandData<ICommand2MC> sd = subcommands.get(subcommand); //O(1)
			if (sd == null) continue;
			Arrays.stream(sd.helpText).skip(1).map(ht -> new HashMap.SimpleEntry<>(ht, subcommands.get(ht))).filter(e -> e.getValue() != null)
				.filter(kv -> kv.getKey().startsWith(commandline))
				.filter(kv -> hasPermission(sender, kv.getValue().command, kv.getValue().method))
				.forEach(kv -> event.getCompletions().add((kv.getKey()).substring(kv.getKey().lastIndexOf(' ', commandline.length()) + 1)));
			if (sd.method == null || sd.command == null)
				return;
			/*if (!hasPermission(sender, sd.command, sd.method)) { - TODO: Arguments
				sender.sendMessage("§cYou don't have permission to use this command");
				return true;
			}
			val params = new ArrayList<Object>(sd.method.getParameterCount());
			int j = subcommand.length(), pj;
			Class<?>[] parameterTypes = sd.method.getParameterTypes();
			if (parameterTypes.length == 0)
				throw new Exception("No sender parameter for method '" + sd.method + "'");
			val sendertype = parameterTypes[0];
			final ChromaGamerBase cg;
			if (sendertype.isAssignableFrom(sender.getClass()))
				params.add(sender); //The command either expects a CommandSender or it is a Player, or some other expected type
			else if (sender instanceof Command2MCSender
				&& sendertype.isAssignableFrom(((Command2MCSender) sender).getSender().getClass()))
				params.add(((Command2MCSender) sender).getSender());
			else if (ChromaGamerBase.class.isAssignableFrom(sendertype)
				&& sender instanceof Command2MCSender
				&& (cg = ChromaGamerBase.getFromSender(((Command2MCSender) sender).getSender())) != null
				&& cg.getClass() == sendertype) //The command expects a user of our system
				params.add(cg);
			else {
				sender.sendMessage("§cYou need to be a " + sendertype.getSimpleName() + " to use this command.");
				return true;
			}
			val paramArr = sd.method.getParameters();
			for (int i1 = 1; i1 < parameterTypes.length; i1++) {
				Class<?> cl = parameterTypes[i1];
				pj = j + 1; //Start index
				if (pj == commandline.length() + 1) { //No param given
					if (paramArr[i1].isAnnotationPresent(OptionalArg.class)) {
						if (cl.isPrimitive())
							params.add(Defaults.defaultValue(cl));
						else if (Number.class.isAssignableFrom(cl)
							|| Number.class.isAssignableFrom(cl))
							params.add(Defaults.defaultValue(Primitives.unwrap(cl)));
						else
							params.add(null);
						continue; //Fill the remaining params with nulls
					} else {
						sender.sendMessage(sd.helpText); //Required param missing
						return true;
					}
				}
				if (paramArr[i1].isVarArgs()) {
					par0ams.add(commandline.substring(j + 1).split(" +"));
					continue;
				}
				j = commandline.indexOf(' ', j + 1); //End index
				if (j == -1 || paramArr[i1].isAnnotationPresent(TextArg.class)) //Last parameter
					j = commandline.length();
				String param = commandline.substring(pj, j);
				if (cl == String.class) {
					params.add(param);
					continue;
				} else if (Number.class.isAssignableFrom(cl) || cl.isPrimitive()) {
					try {
						//noinspection unchecked
						Number n = ThorpeUtils.convertNumber(NumberFormat.getInstance().parse(param), (Class<? extends Number>) cl);
						params.add(n);
					} catch (ParseException e) {
						sender.sendMessage("§c'" + param + "' is not a number.");
						return true;
					}
					continue;
				}
				val conv = paramConverters.get(cl);
				if (conv == null)
					throw new Exception("No suitable converter found for parameter type '" + cl.getCanonicalName() + "' for command '" + sd.method.toString() + "'");
				val cparam = conv.converter.apply(param);
				if (cparam == null) {
					sender.sendMessage(conv.errormsg); //Param conversion failed - ex. plugin not found
					return true;
				}
				params.add(cparam);
			}
			try {
				val ret = sd.method.invoke(sd.command, params.toArray()); //I FORGOT TO TURN IT INTO AN ARRAY (for a long time)
				if (ret instanceof Boolean) {
					if (!(boolean) ret) //Show usage
						sender.sendMessage(sd.helpText);
				} else if (ret != null)
					throw new Exception("Wrong return type! Must return a boolean or void. Return value: " + ret);
				return true; //We found a method
			} catch (InvocationTargetException e) {
				TBMCCoreAPI.SendException("An error occurred in a command handler!", e.getCause());
			}*/
		}
	}

	private boolean shouldRegisterOfficially = true;

	private void registerOfficially(ICommand2MC command, List<SubcommandData<ICommand2MC>> subcmds) {
		if (!shouldRegisterOfficially) return;
		if (CommodoreProvider.isSupported()) {
			TabcompleteHelper.registerTabcomplete(command, subcmds);
			return; //Commodore registers the command as well
		}
		try {
			var cmdmap = (SimpleCommandMap) Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
			var path = command.getCommandPath();
			int x = path.indexOf(' ');
			var mainPath = path.substring(0, x == -1 ? path.length() : x);
			cmdmap.register(command.getPlugin().getName(), new BukkitCommand(mainPath));
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Failed to register command in command map!", e);
			shouldRegisterOfficially = false;
		}
	}

	private static class BukkitCommand extends Command {
		protected BukkitCommand(String name) {
			super(name);
		}

		@Override
		public boolean execute(CommandSender sender, String commandLabel, String[] args) {
			sender.sendMessage("§cThe command wasn't executed for some reason... (command processing failed)");
			return true;
		}
	}

	private static class TabcompleteHelper {
		private static Commodore commodore;

		private static void registerTabcomplete(ICommand2MC command2MC, List<SubcommandData<ICommand2MC>> subcmds) {
			if (commodore == null)
				commodore = CommodoreProvider.getCommodore(MainPlugin.Instance); //Register all to the Core, it's easier
			System.out.println("Registering tabcomplete for path: " + command2MC.getCommandPath());
			String[] path = command2MC.getCommandPath().split(" ");
			var maincmd = LiteralArgumentBuilder.literal(path[0]);
			var cmd = maincmd;
			for (int i = 1; i < path.length; i++) {
				var subcmd = LiteralArgumentBuilder.literal(path[i]);
				cmd.then(subcmd);
				cmd = subcmd; //Add each part of the path as a child of the previous one
			}
			for (SubcommandData<ICommand2MC> subcmd : subcmds) {
				String[] subpath = ButtonPlugin.getCommand2MC().getCommandPath(subcmd.method.getName(), ' ').trim().split(" ");
				ArgumentBuilder<Object, ?> scmd = cmd;
				if (subpath[0].length() > 0) { //If the method is def, it will contain one empty string
					for (String s : subpath) {
						var subsubcmd = LiteralArgumentBuilder.literal(s);
						scmd.then(subsubcmd);
						scmd = subsubcmd; //Add method name part of the path (could_be_multiple())
					}
				}
				Parameter[] parameters = subcmd.method.getParameters();
				for (int i = 1; i < parameters.length; i++) { //Skip sender
					Parameter parameter = parameters[i];
					ArgumentType<?> type;
					final Class<?> ptype = parameter.getType();
					if (ptype == String.class)
						if (parameter.isAnnotationPresent(TextArg.class))
							type = StringArgumentType.greedyString();
						else
							type = StringArgumentType.word();
					else if (ptype == int.class || ptype == Integer.class)
						type = IntegerArgumentType.integer(); //TODO: Min, max
					else if (ptype == long.class || ptype == Long.class)
						type = LongArgumentType.longArg();
					else if (ptype == float.class || ptype == Float.class)
						type = FloatArgumentType.floatArg();
					else if (ptype == double.class || ptype == Double.class)
						type = DoubleArgumentType.doubleArg();
					else if (ptype == char.class || ptype == Character.class)
						type = StringArgumentType.word();
					else if (ptype == boolean.class || ptype == Boolean.class)
						type = BoolArgumentType.bool();
					else  //TODO: Custom parameter types
						type = StringArgumentType.word();
					var arg = RequiredArgumentBuilder.argument(parameter.getName(), type);
					scmd.then(arg);
					scmd = arg;
				}
			}
			System.out.println("maincmd: " + maincmd);
			System.out.println("Children:");
			maincmd.build().getChildren().forEach(System.out::println);
			commodore.register(maincmd);
		}
	}
}
