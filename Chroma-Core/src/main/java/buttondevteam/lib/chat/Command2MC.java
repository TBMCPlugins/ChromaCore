package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.player.ChromaGamerBase;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.val;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.javatuples.Triplet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Command2MC extends Command2<ICommand2MC, Command2MCSender> implements Listener {
	/**
	 * Don't use directly, use the method in Component and ButtonPlugin to automatically unregister the command when needed.
	 *
	 * @param command The command to register
	 */
	@Override
	public void registerCommand(ICommand2MC command) {
		/*String mainpath;
		var plugin = command.getPlugin();
		{
			String cpath = command.getCommandPath();
			int i = cpath.indexOf(' ');
			mainpath = cpath.substring(0, i == -1 ? cpath.length() : i);
		}*/
		var subcmds = super.registerCommand(command, '/');
		var bcmd = registerOfficially(command, subcmds);
		if (bcmd != null)
			for (String alias : bcmd.getAliases())
				super.registerCommand(command, command.getCommandPath().replaceFirst("^" + bcmd.getName(), Matcher.quoteReplacement(alias)), '/');

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
			String permGroup = "chroma." + pg;
			if (Bukkit.getPluginManager().getPermission(permGroup) == null) //It may occur multiple times
				Bukkit.getPluginManager().addPermission(new Permission(permGroup,
					PermissionDefault.OP)); //Do not allow any commands that belong to a group
		}
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

	/*@EventHandler
	public void onTabComplete(TabCompleteEvent event) {
		try {
			event.getCompletions().clear(); //Remove player names
		} catch (UnsupportedOperationException e) {
			//System.out.println("Tabcomplete: " + event.getBuffer());
			//System.out.println("First completion: " + event.getCompletions().stream().findFirst().orElse("no completions"));
			//System.out.println("Listeners: " + Arrays.toString(event.getHandlers().getRegisteredListeners()));
		}
	}*/

	@Override
	public boolean handleCommand(Command2MCSender sender, String commandline) {
		return handleCommand(sender, commandline, true);
	}

	private boolean handleCommand(Command2MCSender sender, String commandline, boolean checkPlugin) {
		int i = commandline.indexOf(' ');
		String mainpath = commandline.substring(1, i == -1 ? commandline.length() : i); //Without the slash
		PluginCommand pcmd;
		/*System.out.println("Command line: " + commandline);
		System.out.println("Prioritize: " + MainPlugin.Instance.prioritizeCustomCommands().get());
		System.out.println("PCMD: " + (pcmd = Bukkit.getPluginCommand(mainpath)));
		if (pcmd != null)
			System.out.println("ButtonPlugin: " + (pcmd.getPlugin() instanceof ButtonPlugin));*/
		if (!checkPlugin
			|| MainPlugin.Instance.prioritizeCustomCommands().get()
			|| (pcmd = Bukkit.getPluginCommand(mainpath)) == null //Our commands aren't PluginCommands
			|| pcmd.getPlugin() instanceof ButtonPlugin) //Unless it's specified in the plugin.yml
			return super.handleCommand(sender, commandline);
		else
			return false;
	}

	private boolean shouldRegisterOfficially = true;

	private Command registerOfficially(ICommand2MC command, List<SubcommandData<ICommand2MC>> subcmds) {
		if (!shouldRegisterOfficially || command.getPlugin() == null) return null;
		try {
			var cmdmap = (SimpleCommandMap) Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
			var path = command.getCommandPath();
			int x = path.indexOf(' ');
			var mainPath = path.substring(0, x == -1 ? path.length() : x);
			Command bukkitCommand;
			{ //TODO: Commands conflicting with Essentials have to be registered in plugin.yml
				var oldcmd = cmdmap.getCommand(command.getPlugin().getName() + ":" + mainPath); //The label with the fallback prefix is always registered
				if (oldcmd == null) {
					bukkitCommand = new BukkitCommand(mainPath);
					cmdmap.register(command.getPlugin().getName(), bukkitCommand);
				} else {
					bukkitCommand = oldcmd;
					if (bukkitCommand instanceof PluginCommand)
						((PluginCommand) bukkitCommand).setExecutor(this::executeCommand);
				}
				bukkitCommand = oldcmd == null ? new BukkitCommand(mainPath) : oldcmd;
				/*System.out.println("oldcmd: " + oldcmd);
				System.out.println("bukkitCommand: " + bukkitCommand);*/
			}
			//System.out.println("Registering to " + command.getPlugin().getName());
			if (CommodoreProvider.isSupported())
				TabcompleteHelper.registerTabcomplete(command, subcmds, bukkitCommand);
			return bukkitCommand;
		} catch (Exception e) {
			if (command.getComponent() == null)
				TBMCCoreAPI.SendException("Failed to register command in command map!", e, command.getPlugin());
			else
				TBMCCoreAPI.SendException("Failed to register command in command map!", e, command.getComponent());
			shouldRegisterOfficially = false;
			return null;
		}
	}

	private boolean executeCommand(CommandSender sender, Command command, String label, String[] args) {
		var user = ChromaGamerBase.getFromSender(sender);
		if (user == null) {
			TBMCCoreAPI.SendException("Failed to run Bukkit command for user!", new Throwable("No Chroma user found"), MainPlugin.Instance);
			sender.sendMessage("§cAn internal error occurred.");
			return true;
		}
		//System.out.println("Executing " + label + " which is actually " + command.getName());
		handleCommand(new Command2MCSender(sender, user.channel().get(), sender),
			("/" + command.getName() + " " + String.join(" ", args)).trim(), false); ///trim(): remove space if there are no args
		return true;
	}

	private static class BukkitCommand extends Command {
		protected BukkitCommand(String name) {
			super(name);
		}

		@Override
		public boolean execute(CommandSender sender, String commandLabel, String[] args) {
			return ButtonPlugin.getCommand2MC().executeCommand(sender, this, commandLabel, args);
		}

		@Override
		public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
			//System.out.println("Correct tabcomplete queried");
			return Collections.emptyList();
		}

		@Override
		public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
			//System.out.println("Correct tabcomplete queried");
			return Collections.emptyList();
		}
	}

	private static class TabcompleteHelper {
		private static Commodore commodore;

		private static LiteralCommandNode<Object> appendSubcommand(String path, CommandNode<Object> parent,
		                                                           SubcommandData<ICommand2MC> subcommand) {
			LiteralCommandNode<Object> scmd;
			if ((scmd = (LiteralCommandNode<Object>) parent.getChild(path)) != null)
				return scmd;
			var scmdBuilder = LiteralArgumentBuilder.literal(path);
			if (subcommand != null)
				scmdBuilder.requires(o -> {
					var sender = commodore.getBukkitSender(o);
					return ButtonPlugin.getCommand2MC().hasPermission(sender, subcommand.command, subcommand.method);
				});
			scmd = scmdBuilder.build();
			parent.addChild(scmd);
			return scmd;
		}

		private static void registerTabcomplete(ICommand2MC command2MC, List<SubcommandData<ICommand2MC>> subcmds, Command bukkitCommand) {
			if (commodore == null) {
				commodore = CommodoreProvider.getCommodore(MainPlugin.Instance); //Register all to the Core, it's easier
				commodore.register(LiteralArgumentBuilder.literal("un").redirect(RequiredArgumentBuilder.argument("unsomething",
					StringArgumentType.word()).suggests((context, builder) -> builder.suggest("untest").buildFuture()).build()));
			}
			String[] path = command2MC.getCommandPath().split(" ");
			var shouldRegister = new AtomicBoolean(true);
			@SuppressWarnings("unchecked") var maincmd = commodore.getRegisteredNodes().stream()
				.filter(node -> node.getLiteral().equalsIgnoreCase(path[0]))
				.filter(node -> { shouldRegister.set(false); return true; })
				.map(node -> (LiteralCommandNode<Object>) node).findAny()
				.orElseGet(() -> LiteralArgumentBuilder.literal(path[0]).build()); //Commodore 1.8 removes previous nodes
			var cmd = maincmd;
			for (int i = 1; i < path.length; i++) {
				var scmd = subcmds.stream().filter(sd -> sd.method.getName().equals("def")).findAny().orElse(null);
				cmd = appendSubcommand(path[i], cmd, scmd);  //Add each part of the path as a child of the previous one
			}
			final var customTCmethods = Arrays.stream(command2MC.getClass().getDeclaredMethods()) //val doesn't recognize the type arguments
				.flatMap(method -> Stream.of(Optional.ofNullable(method.getAnnotation(CustomTabCompleteMethod.class)))
					.filter(Optional::isPresent).map(Optional::get) // Java 9 has .stream()
					.flatMap(ctcm -> {
						var paths = Optional.of(ctcm.subcommand()).filter(s -> s.length > 0)
							.orElseGet(() -> new String[]{
								ButtonPlugin.getCommand2MC().getCommandPath(method.getName(), ' ').trim()
							});
						return Arrays.stream(paths).map(name -> new Triplet<>(name, ctcm, method));
					})).collect(Collectors.toList());
			for (SubcommandData<ICommand2MC> subcmd : subcmds) {
				String subpathAsOne = ButtonPlugin.getCommand2MC().getCommandPath(subcmd.method.getName(), ' ').trim();
				String[] subpath = subpathAsOne.split(" ");
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
					final boolean customParamType;
					{
						boolean customParamTypeTemp = false;
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
						else if (parameter.isVarArgs())
							type = StringArgumentType.greedyString();
						else {
							type = StringArgumentType.word();
							customParamTypeTemp = true;
						}
						customParamType = customParamTypeTemp;
					}
					val param = subcmd.parameters[i - 1];
					val customTC = Optional.ofNullable(parameter.getAnnotation(CustomTabComplete.class))
						.map(CustomTabComplete::value);
					var customTCmethod = customTCmethods.stream().filter(t -> subpathAsOne.equalsIgnoreCase(t.getValue0()))
						.filter(t -> param.replaceAll("[\\[\\]<>]", "").equalsIgnoreCase(t.getValue1().param()))
						.findAny();
					var argb = RequiredArgumentBuilder.argument(param, type)
						.suggests((context, builder) -> {
							if (parameter.isVarArgs()) { //Do it before the builder is used
								int nextTokenStart = context.getInput().lastIndexOf(' ') + 1;
								builder = builder.createOffset(nextTokenStart);
							}
							if (customTC.isPresent())
								for (val ctc : customTC.get())
									builder.suggest(ctc);
							boolean ignoreCustomParamType = false;
							if (customTCmethod.isPresent()) {
								var tr = customTCmethod.get();
								if (tr.getValue1().ignoreTypeCompletion())
									ignoreCustomParamType = true;
								final var method = tr.getValue2();
								val params = method.getParameters();
								val args = new Object[params.length];
								for (int j = 0, k = 0; j < args.length && k < subcmd.parameters.length; j++) {
									val paramObj = params[j];
									if (CommandSender.class.isAssignableFrom(paramObj.getType())) {
										args[j] = commodore.getBukkitSender(context.getSource());
										continue;
									}
									val paramValueString = context.getArgument(subcmd.parameters[k], String.class);
									if (paramObj.getType() == String.class) {
										args[j] = paramValueString;
										continue;
									}
									val converter = getParamConverter(params[j].getType(), command2MC);
									if (converter == null)
										break;
									val paramValue = converter.converter.apply(paramValueString);
									if (paramValue == null) //For example, the player provided an invalid plugin name
										break;
									args[j] = paramValue;
									k++; //Only increment if not CommandSender
								}
								if (args.length == 0 || args[args.length - 1] != null) { //Arguments filled entirely
									try {
										val suggestions = method.invoke(command2MC, args);
										if (suggestions instanceof Iterable) {
											//noinspection unchecked
											for (Object suggestion : (Iterable<Object>) suggestions)
												if (suggestion instanceof String)
													builder.suggest((String) suggestion);
												else
													throw new ClassCastException("Bad return type! It should return an Iterable<String> or a String[].");
										} else if (suggestions instanceof String[])
											for (String suggestion : (String[]) suggestions)
												builder.suggest(suggestion);
										else
											throw new ClassCastException("Bad return type! It should return a String[] or an Iterable<String>.");
									} catch (Exception e) {
										String msg = "Failed to run tabcomplete method " + method.getName() + " for command " + command2MC.getClass().getSimpleName();
										if (command2MC.getComponent() == null)
											TBMCCoreAPI.SendException(msg, e, command2MC.getPlugin());
										else
											TBMCCoreAPI.SendException(msg, e, command2MC.getComponent());
									}
								}
							}
							if (!ignoreCustomParamType && customParamType) {
								val converter = getParamConverter(ptype, command2MC);
								if (converter != null) {
									var suggestions = converter.allSupplier.get();
									for (String suggestion : suggestions)
										builder.suggest(suggestion);
								}
							}
							if (ptype == boolean.class || ptype == Boolean.class)
								builder.suggest("true").suggest("false");
							final String loweredInput = builder.getRemaining().toLowerCase();
							return builder.suggest(param).buildFuture().whenComplete((s, e) -> //The list is automatically ordered
								s.getList().add(s.getList().remove(0))) //So we need to put the <param> at the end after that
								.whenComplete((ss, e) -> ss.getList().removeIf(s -> {
									String text = s.getText();
									return !text.startsWith("<") && !text.startsWith("[") && !text.toLowerCase().startsWith(loweredInput);
								}));
						});
					var arg = argb.build();
					scmd.addChild(arg);
					scmd = arg;
				}
			}
			if (shouldRegister.get()) {
				commodore.register(maincmd);
				//MinecraftArgumentTypes.getByKey(NamespacedKey.minecraft(""))
				var prefixedcmd = new LiteralCommandNode<>(command2MC.getPlugin().getName().toLowerCase() + ":" + path[0], maincmd.getCommand(), maincmd.getRequirement(), maincmd.getRedirect(), maincmd.getRedirectModifier(), maincmd.isFork());
				for (var child : maincmd.getChildren())
					prefixedcmd.addChild(child);
				commodore.register(prefixedcmd);
			}
		}
	}

	private static ParamConverter<?> getParamConverter(Class<?> cl, ICommand2MC command2MC) {
		val converter = ButtonPlugin.getCommand2MC().paramConverters.get(cl);
		if (converter == null) {
			String msg = "Could not find a suitable converter for type " + cl.getSimpleName();
			Exception exception = new NullPointerException("converter is null");
			if (command2MC.getComponent() == null)
				TBMCCoreAPI.SendException(msg, exception, command2MC.getPlugin());
			else
				TBMCCoreAPI.SendException(msg, exception, command2MC.getComponent());
			return null;
		}
		return converter;
	}
}
