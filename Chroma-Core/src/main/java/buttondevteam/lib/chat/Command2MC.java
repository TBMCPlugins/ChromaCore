package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.architecture.Component;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.val;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
	public <T> void addParamConverter(Class<T> cl, Function<String, T> converter, String errormsg, Supplier<Iterable<String>> allSupplier) {
		super.addParamConverter(cl, converter, "§c" + errormsg, allSupplier);
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
	public void onTabComplete(TabCompleteEvent event) {
		//System.out.println("Tabcomplete: " + event.getBuffer());
		//System.out.println("First completion: " + event.getCompletions().stream().findFirst().orElse("no completions"));
		event.getCompletions().clear();
	}

	private boolean shouldRegisterOfficially = true;

	private void registerOfficially(ICommand2MC command, List<SubcommandData<ICommand2MC>> subcmds) {
		if (!shouldRegisterOfficially) return;
		try {
			var cmdmap = (SimpleCommandMap) Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
			var path = command.getCommandPath();
			int x = path.indexOf(' ');
			var mainPath = path.substring(0, x == -1 ? path.length() : x);
			var bukkitCommand = new BukkitCommand(mainPath);
			cmdmap.register(command.getPlugin().getName(), bukkitCommand);
			if (CommodoreProvider.isSupported())
				TabcompleteHelper.registerTabcomplete(command, subcmds, bukkitCommand);
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

		@Override
		public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
			return Collections.emptyList();
		}

		@Override
		public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
			return Collections.emptyList();
		}
	}

	private static class TabcompleteHelper {
		private static Commodore commodore;

		private static LiteralCommandNode<Object> appendSubcommand(String path, CommandNode<Object> parent,
		                                                           SubcommandData<ICommand2MC> subcommand) {
			var scmdBuilder = LiteralArgumentBuilder.literal(path);
			if (subcommand != null)
				scmdBuilder.requires(o -> {
					var sender = commodore.getBukkitSender(o);
					return ButtonPlugin.getCommand2MC().hasPermission(sender, subcommand.command, subcommand.method);
				});
			var scmd = scmdBuilder.build();
			parent.addChild(scmd);
			return scmd;
		}

		private static void registerTabcomplete(ICommand2MC command2MC, List<SubcommandData<ICommand2MC>> subcmds, Command bukkitCommand) {
			if (commodore == null)
				commodore = CommodoreProvider.getCommodore(MainPlugin.Instance); //Register all to the Core, it's easier
			String[] path = command2MC.getCommandPath().split(" ");
			var maincmd = LiteralArgumentBuilder.literal(path[0]).build();
			var cmd = maincmd;
			for (int i = 1; i < path.length; i++) {
				var scmd = subcmds.stream().filter(sd -> sd.method.getName().equals("def")).findAny().orElse(null);
				cmd = appendSubcommand(path[i], cmd, scmd);  //Add each part of the path as a child of the previous one
			}
			for (SubcommandData<ICommand2MC> subcmd : subcmds) {
				String[] subpath = ButtonPlugin.getCommand2MC().getCommandPath(subcmd.method.getName(), ' ').trim().split(" ");
				CommandNode<Object> scmd = cmd;
				if (subpath[0].length() > 0) { //If the method is def, it will contain one empty string
					for (String s : subpath) {
						scmd = appendSubcommand(s, scmd, subcmd); //Add method name part of the path (could_be_multiple())
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
					else if (ptype == int.class || ptype == Integer.class
						|| ptype == byte.class || ptype == Byte.class
						|| ptype == short.class || ptype == Short.class)
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
					val param = subcmd.parameters[i - 1];
					var argb = RequiredArgumentBuilder.argument(param, type)
						.suggests((SuggestionProvider<Object>) (context, builder) -> {
							//TODO
							return builder.suggest(param).buildFuture();
						});
					var arg = argb.build();
					scmd.addChild(arg);
					scmd = arg;
				}
			}
			/*try {
				Class.forName("net.minecraft.server.v1_15_R1.ArgumentRegistry").getMethod("a", String.class, Class.class,
					Class.forName("net.minecraft.server.v1_15_R1.ArgumentSerializer"))
					.invoke(null, "chroma:string", BetterStringArgumentType.class,
						Class.forName("net.minecraft.server.v1_15_R1.ArgumentSerializerVoid").getConstructors()[0]
							.newInstance((Supplier<BetterStringArgumentType>) BetterStringArgumentType::word));
			} catch (Exception e) { - Client log: Could not deserialize chroma:string
				e.printStackTrace();
			}*/
			commodore.register(maincmd);
		}
	}
}
