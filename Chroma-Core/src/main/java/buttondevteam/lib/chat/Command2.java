package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.commands.CommandArgument;
import buttondevteam.lib.chat.commands.ParameterData;
import buttondevteam.lib.chat.commands.SubcommandData;
import buttondevteam.lib.player.ChromaGamerBase;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static buttondevteam.lib.chat.CoreCommandBuilder.literal;

/**
 * The method name is the subcommand, use underlines (_) to add further subcommands.
 * The args may be null if the conversion failed and it's optional.
 */
@RequiredArgsConstructor
public abstract class Command2<TC extends ICommand2<TP>, TP extends Command2Sender> {
	/**
	 * Parameters annotated with this receive all the remaining arguments
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TextArg {
	}

	/**
	 * Methods annotated with this will be recognised as subcommands
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Subcommand {
		/**
		 * Allowed for OPs only by default
		 */
		String MOD_GROUP = "mod";

		/**
		 * Help text to show players. A usage message will be also shown below it.
		 */
		String[] helpText() default {};

		/**
		 * The main permission which allows using this command (individual access can be still revoked with "chroma.command.X").
		 * Used to be "tbmc.admin". The {@link #MOD_GROUP} is provided to use with this.
		 */
		String permGroup() default "";

		String[] aliases() default {};
	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface OptionalArg {
	}

	/*protected static class SubcommandHelpData<T extends ICommand2> extends SubcommandData<T> {
		private final TreeSet<String> ht = new TreeSet<>();
		private BukkitTask task;

		public SubcommandHelpData(Method method, T command, String[] helpText) {
			super(method, command, helpText);
		}

		public void addSubcommand(String command) {
			ht.add(command);
			if (task == null)
				task = Bukkit.getScheduler().runTask(MainPlugin.Instance, () -> {
					helpText = new String[ht.size() + 1]; //This will only run after the server is started  List<E> list = new ArrayList<E>(size());
					helpText[0] = "§6---- Subcommands ----"; //TODO: There may be more to the help text
					int i = 1;
					for (Iterator<String> iterator = ht.iterator();
					     iterator.hasNext() && i < helpText.length; i++) {
						String e = iterator.next();
						helpText[i] = e;
					}
					task = null; //Run again, if needed
				});
		}
	}*/

	@RequiredArgsConstructor
	protected static class ParamConverter<T> {
		public final Function<String, T> converter;
		public final String errormsg;
		public final Supplier<Iterable<String>> allSupplier;
	}

	protected final HashMap<Class<?>, ParamConverter<?>> paramConverters = new HashMap<>();
	private final ArrayList<String> commandHelp = new ArrayList<>(); //Mainly needed by Discord
	private final CommandDispatcher<TP> dispatcher = new CommandDispatcher<>();

	private final char commandChar;

	/**
	 * Adds a param converter that obtains a specific object from a string parameter.
	 * The converter may return null.
	 *
	 * @param <T>         The type of the result
	 * @param cl          The class of the result object
	 * @param converter   The converter to use
	 * @param allSupplier The supplier of all possible values (ideally)
	 */
	public <T> void addParamConverter(Class<T> cl, Function<String, T> converter, String errormsg,
	                                  Supplier<Iterable<String>> allSupplier) {
		paramConverters.put(cl, new ParamConverter<>(converter, errormsg, allSupplier));
	}

	public boolean handleCommand(TP sender, String commandline) {
		var results = dispatcher.parse(commandline, sender);
		boolean sync = Bukkit.isPrimaryThread();
		Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.Instance, () -> {
			try {
				handleCommandAsync(sender, results, results.getContext().getNodes(), sync);
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Command execution failed for sender " + sender.getName() + "(" + sender.getClass().getCanonicalName() + ") and message " + commandline, e, MainPlugin.Instance);
			}
		});
		return true; //We found a method - TODO
	}

	//Needed because permission checking may load the (perhaps offline) sender's file which is disallowed on the main thread

	/**
	 * Handles a command asynchronously
	 *
	 * @param sender      The command sender
	 * @param commandNode The processed command the sender sent
	 * @param sd          The subcommand data
	 * @param sync        Whether the command was originally sync
	 */
	private void handleCommandAsync(TP sender, ParseResults<?> parsed, SubcommandData<TC> sd, boolean sync) {
		if (sd.method == null || sd.command == null) { //Main command not registered, but we have subcommands
			sender.sendMessage(sd.helpText);
			return;
		}
		if (!hasPermission(sender, sd.command, sd.method)) {
			sender.sendMessage("§cYou don't have permission to use this command");
			return;
		}
		// TODO: WIP
		if (processSenderType(sender, sd, params, parameterTypes)) return; // Checks if the sender is the wrong type
		val args = parsed.getContext().getArguments();
		for (var arg : sd.arguments.entrySet()) {
			// TODO: Invoke using custom method
			/*if (pj == commandline.length() + 1) { //No param given
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
					return;
				}
			}*/
			/*if (paramArr[i1].isVarArgs()) { - TODO: Varargs support? (colors?)
				params.add(commandline.substring(j + 1).split(" +"));
				continue;
			}*/
			// TODO: Character handling (strlen)
			// TODO: Param converter
		}
		Runnable invokeCommand = () -> {
			try {
				sd.method.setAccessible(true); //It may be part of a private class
				val ret = sd.method.invoke(sd.command, params.toArray()); //I FORGOT TO TURN IT INTO AN ARRAY (for a long time)
				if (ret instanceof Boolean) {
					if (!(boolean) ret) //Show usage
						sender.sendMessage(sd.helpText);
				} else if (ret != null)
					throw new Exception("Wrong return type! Must return a boolean or void. Return value: " + ret);
			} catch (InvocationTargetException e) {
				TBMCCoreAPI.SendException("An error occurred in a command handler for " + subcommand + "!", e.getCause(), MainPlugin.Instance);
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Command handling failed for sender " + sender + " and subcommand " + subcommand, e, MainPlugin.Instance);
			}
		};
		if (sync)
			Bukkit.getScheduler().runTask(MainPlugin.Instance, invokeCommand);
		else
			invokeCommand.run();
	} //TODO: Add to the help

	private boolean processSenderType(TP sender, SubcommandData<TC> sd, ArrayList<Object> params) {
		val sendertype = sd.senderType;
		final ChromaGamerBase cg;
		if (sendertype.isAssignableFrom(sender.getClass()))
			params.add(sender); //The command either expects a CommandSender or it is a Player, or some other expected type
		else if (sender instanceof Command2MCSender // TODO: This is Minecraft only
			&& sendertype.isAssignableFrom(((Command2MCSender) sender).getSender().getClass()))
			params.add(((Command2MCSender) sender).getSender());
		else if (ChromaGamerBase.class.isAssignableFrom(sendertype)
			&& sender instanceof Command2MCSender
			&& (cg = ChromaGamerBase.getFromSender(((Command2MCSender) sender).getSender())) != null
			&& cg.getClass() == sendertype) //The command expects a user of our system
			params.add(cg);
		else {
			String type = sendertype.getSimpleName().chars().mapToObj(ch -> Character.isUpperCase(ch)
				? " " + Character.toLowerCase(ch)
				: ch + "").collect(Collectors.joining());
			sender.sendMessage("§cYou need to be a " + type + " to use this command.");
			sender.sendMessage(sd.getHelpText(sender)); //Send what the command is about, could be useful for commands like /member where some subcommands aren't player-only
			return true;
		}
		return false;
	}

	/**
	 * Constructs a command node for the given subcommand that can be used for a custom registering logic (Discord).
	 *
	 * @param command The command object
	 * @param method  The subcommand method
	 * @return The processed command node
	 * @throws Exception Something broke
	 */
	protected LiteralCommandNode<TP> processSubcommand(TC command, Method method) throws Exception {
		val params = new ArrayList<Object>(method.getParameterCount());
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length == 0)
			throw new Exception("No sender parameter for method '" + method + "'");
		val paramArr = method.getParameters();
		val arguments = new HashMap<String, CommandArgument>(parameterTypes.length - 1);
		for (int i1 = 1; i1 < parameterTypes.length; i1++) {
			Class<?> cl = parameterTypes[i1];
			var pdata = getParameterData(method, i1);
			arguments.put(pdata.name, new CommandArgument(pdata.name, cl, pdata.description));
		}
		var sd = new SubcommandData<TC>(parameterTypes[0], arguments, command, command.getHelpText(method, method.getAnnotation(Subcommand.class)), null); // TODO: Help text
		return getSubcommandNode(method, sd); // TODO: Integrate with getCommandNode and store SubcommandData instead of help text
	}

	/**
	 * Get parameter data for the given subcommand. Attempts to read it from the commands file, if it fails, it will return generic info.
	 *
	 * @param method The method the subcommand is created from
	 * @param i      The index to use if no name was found
	 * @return Parameter data object
	 */
	private ParameterData getParameterData(Method method, int i) {
		return null; // TODO: Parameter data (from help text method)
	}

	/**
	 * Register a command in the command system. The way this command gets registered may change depending on the implementation.
	 * Always invoke {@link #registerCommandSuper(ICommand2)} when implementing this method.
	 *
	 * @param command The command to register
	 */
	public abstract void registerCommand(TC command);

	/**
	 * Registers a command in the Command2 system, so it can be looked up and executed.
	 *
	 * @param command The command to register
	 * @return The Brigadier command node if you need it for something (like tab completion)
	 */
	protected LiteralCommandNode<TP> registerCommandSuper(TC command) {
		return dispatcher.register(getCommandNode(command));
	}

	private LiteralArgumentBuilder<TP> getCommandNode(TC command) {
		var path = command.getCommandPath().split(" ");
		if (path.length == 0)
			throw new IllegalArgumentException("Attempted to register a command with no command path!");
		CoreCommandBuilder<TP> inner = literal(path[0]);
		var outer = inner;
		for (int i = path.length - 1; i >= 0; i--) {
			CoreCommandBuilder<TP> literal = literal(path[i]);
			outer = (CoreCommandBuilder<TP>) literal.executes(this::executeHelpText).then(outer);
		}
		var subcommandMethods = command.getClass().getMethods();
		for (var subcommandMethod : subcommandMethods) {
			var ann = subcommandMethod.getAnnotation(Subcommand.class);
			if (ann == null) continue;
			inner.then(getSubcommandNode(subcommandMethod, ann.helpText()));
		}
		return outer;
	}

	private LiteralArgumentBuilder<TP> getSubcommandNode(Method method, String[] helpText) {
		CoreCommandBuilder<TP> ret = literal(method.getName());
		return ret.helps(helpText).executes(this::executeCommand);
	}

	private CoreArgumentBuilder<TP, ?> getCommandParameters(Parameter[] parameters) {
		return null; // TODO
	}

	private int executeHelpText(CommandContext<TP> context) {
		System.out.println("Nodes:\n" + context.getNodes().stream().map(node -> node.getNode().getName() + "@" + node.getRange()).collect(Collectors.joining("\n")));
		return 0;
	}

	private int executeCommand(CommandContext<TP> context) {
		System.out.println("Execute command");
		return 0;
	}

	/*protected List<SubcommandData<TC>> registerCommand(TC command, @SuppressWarnings("SameParameterValue") char commandChar) {
		this.commandChar = commandChar;
		Method mainMethod = null;
		boolean nosubs = true;
		boolean isSubcommand = x != -1;
		try { //Register the default handler first so it can be reliably overwritten
			mainMethod = command.getClass().getMethod("def", Command2Sender.class);
			val cc = command.getClass().getAnnotation(CommandClass.class);
			var ht = cc == null || isSubcommand ? new String[0] : cc.helpText(); //If it's not the main command, don't add it
			if (ht.length > 0)
				ht[0] = "§6---- " + ht[0] + " ----";
			scmdHelpList.addAll(Arrays.asList(ht));
			if (!isSubcommand)
				scmdHelpList.add("§6Subcommands:");
			if (!commandHelp.contains(mainPath))
				commandHelp.add(mainPath);
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Could not register default handler for command /" + path, e, MainPlugin.Instance);
		}
		var addedSubcommands = new ArrayList<SubcommandData<TC>>();
		for (val method : command.getClass().getMethods()) {
			val ann = method.getAnnotation(Subcommand.class);
			if (ann == null) continue; //Don't call the method on non-subcommands because they're not in the yaml
			var ht = command.getHelpText(method, ann);
			if (ht != null) { //The method is a subcommand
				val subcommand = commandChar + path + //Add command path (class name by default)
					getCommandPath(method.getName(), ' '); //Add method name, unless it's 'def'
				var params = new String[method.getParameterCount() - 1];
				ht = getParameterHelp(method, ht, subcommand, params);
				var sd = new SubcommandData<>(method, command, params, ht);
				registerCommand(path, method.getName(), ann, sd);
				for (String p : command.getCommandPaths())
					registerCommand(p, method.getName(), ann, sd);
				addedSubcommands.add(sd);
				scmdHelpList.add(subcommand);
				nosubs = false;
			}
		}
		if (nosubs && scmdHelpList.size() > 0)
			scmdHelpList.remove(scmdHelpList.size() - 1); //Remove Subcommands header
		if (mainMethod != null && !subcommands.containsKey(commandChar + path)) { //Command specified by the class
			var sd = new SubcommandData<>(mainMethod, command, null, scmdHelpList.toArray(new String[0]));
			subcommands.put(commandChar + path, sd);
			addedSubcommands.add(sd);
		}
		if (isSubcommand) { //The class itself is a subcommand
			val scmd = subcommands.computeIfAbsent(mainPath, p -> new SubcommandData<>(null, null, new String[0], new String[]{"§6---- Subcommands ----"}));
			val scmdHelp = Arrays.copyOf(scmd.helpText, scmd.helpText.length + scmdHelpList.size());
			for (int i = 0; i < scmdHelpList.size(); i++)
				scmdHelp[scmd.helpText.length + i] = scmdHelpList.get(i);
			scmd.helpText = scmdHelp;
		}
		return addedSubcommands;
	}*/

	private String[] getParameterHelp(Method method, String[] ht, String subcommand, String[] parameters) {
		val str = method.getDeclaringClass().getResourceAsStream("/commands.yml");
		if (str == null)
			TBMCCoreAPI.SendException("Error while getting command data!", new Exception("Resource not found!"), MainPlugin.Instance);
		else {
			if (ht.length > 0)
				ht[0] = "§6---- " + ht[0] + " ----";
			YamlConfiguration yc = YamlConfiguration.loadConfiguration(new InputStreamReader(str)); //Generated by ButtonProcessor
			val ccs = yc.getConfigurationSection(method.getDeclaringClass().getCanonicalName().replace('$', '.'));
			if (ccs != null) {
				val cs = ccs.getConfigurationSection(method.getName());
				if (cs != null) {
					val mname = cs.getString("method");
					val params = cs.getString("params");
					//val goodname = method.getName() + "(" + Arrays.stream(method.getGenericParameterTypes()).map(cl -> cl.getTypeName()).collect(Collectors.joining(",")) + ")";
					int i = mname.indexOf('('); //Check only the name - the whole method is still stored for backwards compatibility and in case it may be useful
					if (i != -1 && method.getName().equals(mname.substring(0, i)) && params != null) {
						String[] both = Arrays.copyOf(ht, ht.length + 1);
						both[ht.length] = "§6Usage:§r " + subcommand + " " + params;
						ht = both;
						var paramArray = params.split(" ");
						for (int j = 0; j < paramArray.length && j < parameters.length; j++)
							parameters[j] = paramArray[j];
					} else
						TBMCCoreAPI.SendException("Error while getting command data for " + method + "!", new Exception("Method '" + method + "' != " + mname + " or params is " + params), MainPlugin.Instance);
				} else
					MainPlugin.Instance.getLogger().warning("Failed to get command data for " + method + " (cs is null)! Make sure to use 'clean install' when building the project.");
			} else
				MainPlugin.Instance.getLogger().warning("Failed to get command data for " + method + " (ccs is null)! Make sure to use 'clean install' when building the project.");
		}
		return ht;
	}

	private void registerCommand(String path, String methodName, Subcommand ann, SubcommandData<TC> sd) {
		val subcommand = commandChar + path + getCommandPath(methodName, ' ');
		subcommands.put(subcommand, sd);
		for (String alias : ann.aliases())
			subcommands.put(commandChar + path + alias, sd);
	}

	public abstract boolean hasPermission(TP sender, TC command, Method subcommand);

	public String[] getCommandsText() {
		return commandHelp.toArray(new String[0]);
	}

	public String[] getHelpText(String path) {
		val scmd = subcommands.get(path);
		if (scmd == null) return null;
		return scmd.helpText;
	}

	/*public Set<String> getAllSubcommands() {
		return Collections.unmodifiableSet(subcommands.keySet());
	}*/

	/**
	 * Unregisters all of the subcommands in the given command.
	 *
	 * @param command The command object
	 */
	public void unregisterCommand(ICommand2<TP> command) {
		var path = command.getCommandPath();
		for (val method : command.getClass().getMethods()) {
			val ann = method.getAnnotation(Subcommand.class);
			if (ann == null) continue;
			unregisterCommand(path, method.getName(), ann);
			for (String p : command.getCommandPaths())
				unregisterCommand(p, method.getName(), ann);
		}
	}

	private void unregisterCommand(String path, String methodName, Subcommand ann) {
		val subcommand = commandChar + path + getCommandPath(methodName, ' ');
		subcommands.remove(subcommand);
		for (String alias : ann.aliases())
			subcommands.remove(commandChar + path + alias);
	}

	/**
	 * It will start with the given replace char.
	 *
	 * @param methodName  The method's name, method.getName()
	 * @param replaceChar The character to use between subcommands
	 * @return The command path starting with the replace char.
	 */
	@NotNull
	public String getCommandPath(String methodName, char replaceChar) {
		return methodName.equals("def") ? "" : replaceChar + methodName.replace('_', replaceChar).toLowerCase();
	}
}
