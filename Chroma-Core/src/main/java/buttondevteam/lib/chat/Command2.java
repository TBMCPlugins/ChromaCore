package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.commands.CommandArgument;
import buttondevteam.lib.chat.commands.NumberArg;
import buttondevteam.lib.chat.commands.SubcommandData;
import buttondevteam.lib.player.ChromaGamerBase;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

	/**
	 * The first character in the command line that shows that it's a command.
	 */
	private final char commandChar;
	/**
	 * Whether the command's actual code has to be run on the primary thread.
	 */
	private final boolean runOnPrimaryThread;

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
		if (results.getReader().canRead()) {
			return false; // Unknown command
		}
		Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.Instance, () -> {
			try {
				dispatcher.execute(results);
			} catch (CommandSyntaxException e) {
				sender.sendMessage(e.getMessage());
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Command execution failed for sender " + sender.getName() + "(" + sender.getClass().getCanonicalName() + ") and message " + commandline, e, MainPlugin.Instance);
			}
		});
		return true; //We found a method
	}

	//Needed because permission checking may load the (perhaps offline) sender's file which is disallowed on the main thread

	//TODO: Add to the help

	private boolean processSenderType(TP sender, SubcommandData<TC, TP> sd, ArrayList<Object> params) {
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
		LiteralCommandNode<TP> mainCommandNode = null;
		for (val meth : command.getClass().getMethods()) {
			val ann = meth.getAnnotation(Subcommand.class);
			if (ann == null) continue;
			String methodPath = getCommandPath(meth.getName(), ' ');
			val result = registerNodeFromPath(command.getCommandPath() + methodPath);
			result.getValue0().addChild(getExecutableNode(meth, command, ann, result.getValue2()));
			if (mainCommandNode == null) mainCommandNode = result.getValue1();
			else if (!result.getValue1().getName().equals(mainCommandNode.getName())) {
				MainPlugin.Instance.getLogger().warning("Multiple commands are defined in the same class! This is not supported. Class: " + command.getClass().getSimpleName());
			}
		}
		if (mainCommandNode == null) {
			throw new RuntimeException("There are no subcommands defined in the command class " + command.getClass().getSimpleName() + "!");
		}
		return mainCommandNode;
	}

	/**
	 * Returns the node that can actually execute the given subcommand.
	 *
	 * @param method  The subcommand method
	 * @param command The command object
	 * @param path    The command path
	 * @return The executable node
	 */
	private LiteralCommandNode<TP> getExecutableNode(Method method, TC command, Subcommand ann, String path) {
		val paramsAndSenderType = getCommandParameters(method); // Param order is important
		val params = paramsAndSenderType.getValue0();
		val paramMap = new HashMap<String, CommandArgument>();
		for (val param : params) {
			paramMap.put(param.name, param);
		}
		val node = CoreCommandBuilder.<TP, TC>literal(path, params[0].type, paramMap, params, command)
			.helps(command.getHelpText(method, ann)).permits(sender -> hasPermission(sender, command, method))
			.executes(this::executeCommand);
		ArgumentBuilder<TP, ?> parent = node;
		for (val param : params) { // Register parameters in the right order
			parent.then(parent = CoreArgumentBuilder.argument(param.name, getParameterType(param), param.optional));
		}
		return node.build();
	}

	/**
	 * Registers all necessary no-op nodes for the given path.
	 *
	 * @param path The full command path
	 * @return The last no-op node that can be used to register the executable node,
	 * the main command node and the last part of the command path (that isn't registered yet)
	 */
	private Triplet<CommandNode<TP>, LiteralCommandNode<TP>, String> registerNodeFromPath(String path) {
		String[] split = path.split(" ");
		CommandNode<TP> parent = dispatcher.getRoot();
		LiteralCommandNode<TP> mainCommand = null;
		for (int i = 0; i < split.length - 1; i++) {
			String part = split[i];
			var child = parent.getChild(part);
			if (child == null)
				parent.addChild(parent = CoreCommandBuilder.<TP, TC>literalNoOp(part).executes(this::executeHelpText).build());
			else parent = child;
			if (i == 0) mainCommand = (LiteralCommandNode<TP>) parent; // Has to be a literal, if not, well, error
		}
		return new Triplet<>(parent, mainCommand, split[split.length - 1]);
	}

	/**
	 * Get parameter data for the given subcommand. Attempts to read it from the commands file, if it fails, it will return generic info.
	 * The first parameter is always the sender both in the methods themselves and in the returned array.
	 *
	 * @param method The method the subcommand is created from
	 * @return Parameter data objects and the sender type
	 * @throws RuntimeException If there is no sender parameter declared in the method
	 */
	private Pair<CommandArgument[], Class<?>> getCommandParameters(Method method) {
		val parameters = method.getParameters();
		if (parameters.length == 0)
			throw new RuntimeException("No sender parameter for method '" + method + "'");
		val ret = new CommandArgument[parameters.length];
		val usage = getParameterHelp(method);
		if (usage == null) {
			for (int i = 1; i < parameters.length; i++) {
				ret[i - 1] = new CommandArgument("param" + i, parameters[i].getType(), false, null, false, "param" + i);
			}
		} else {
			val paramNames = usage.split(" ");
			for (int i = 1; i < parameters.length; i++) {
				val numAnn = parameters[i].getAnnotation(NumberArg.class);
				ret[i - 1] = new CommandArgument(paramNames[i], parameters[i].getType(),
					parameters[i].isVarArgs() || parameters[i].isAnnotationPresent(TextArg.class),
					numAnn == null ? null : new Pair<>(numAnn.lowerLimit(), numAnn.upperLimit()),
					parameters[i].isAnnotationPresent(OptionalArg.class),
					paramNames[i]); // TODO: Description (JavaDoc?)
			}
		}
		return new Pair<>(ret, parameters[0].getType());
	}

	private ArgumentType<?> getParameterType(CommandArgument arg) {
		final Class<?> ptype = arg.type;
		Number lowerLimit = Double.NEGATIVE_INFINITY, upperLimit = Double.POSITIVE_INFINITY;
		if (arg.greedy)
			return StringArgumentType.greedyString();
		else if (ptype == String.class)
			return StringArgumentType.word();
		else if (ptype == int.class || ptype == Integer.class
			|| ptype == byte.class || ptype == Byte.class
			|| ptype == short.class || ptype == Short.class)
			return IntegerArgumentType.integer(lowerLimit.intValue(), upperLimit.intValue());
		else if (ptype == long.class || ptype == Long.class)
			return LongArgumentType.longArg(lowerLimit.longValue(), upperLimit.longValue());
		else if (ptype == float.class || ptype == Float.class)
			return FloatArgumentType.floatArg(lowerLimit.floatValue(), upperLimit.floatValue());
		else if (ptype == double.class || ptype == Double.class)
			return DoubleArgumentType.doubleArg(lowerLimit.doubleValue(), upperLimit.doubleValue());
		else if (ptype == char.class || ptype == Character.class)
			return StringArgumentType.word();
		else if (ptype == boolean.class || ptype == Boolean.class)
			return BoolArgumentType.bool();
		else {
			return StringArgumentType.word();
		}
	}

	private int executeHelpText(CommandContext<TP> context) {
		System.out.println("Nodes:\n" + context.getNodes().stream().map(node -> node.getNode().getName() + "@" + node.getRange()).collect(Collectors.joining("\n")));
		return 0;
	}

	private int executeCommand(CommandContext<TP> context) {
		System.out.println("Execute command");
		System.out.println("Should be running sync: " + runOnPrimaryThread);

		/*if (!hasPermission(sender, sd.command, sd.method)) {
			sender.sendMessage("§cYou don't have permission to use this command");
			return;
		}
		// TODO: WIP
		if (processSenderType(sender, sd, params, parameterTypes)) return; // Checks if the sender is the wrong type
		val args = parsed.getContext().getArguments();
		for (var arg : sd.arguments.entrySet()) {*/
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
		/*}
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
			invokeCommand.run();*/
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

	private String getParameterHelp(Method method) {
		val str = method.getDeclaringClass().getResourceAsStream("/commands.yml");
		if (str == null)
			TBMCCoreAPI.SendException("Error while getting command data!", new Exception("Resource not found!"), MainPlugin.Instance);
		else {
			YamlConfiguration yc = YamlConfiguration.loadConfiguration(new InputStreamReader(str)); //Generated by ButtonProcessor
			val ccs = yc.getConfigurationSection(method.getDeclaringClass().getCanonicalName().replace('$', '.'));
			if (ccs != null) {
				val cs = ccs.getConfigurationSection(method.getName());
				if (cs != null) {
					val mname = cs.getString("method");
					val params = cs.getString("params");
					int i = mname.indexOf('('); //Check only the name - the whole method is still stored for backwards compatibility and in case it may be useful
					if (i != -1 && method.getName().equals(mname.substring(0, i)) && params != null) {
						return params;
					} else
						TBMCCoreAPI.SendException("Error while getting command data for " + method + "!", new Exception("Method '" + method + "' != " + mname + " or params is " + params), MainPlugin.Instance);
				} else
					MainPlugin.Instance.getLogger().warning("Failed to get command data for " + method + " (cs is null)! Make sure to use 'clean install' when building the project.");
			} else
				MainPlugin.Instance.getLogger().warning("Failed to get command data for " + method + " (ccs is null)! Make sure to use 'clean install' when building the project.");
		}
		return null;
	}

	public abstract boolean hasPermission(TP sender, TC command, Method subcommand);

	public String[] getCommandsText() {
		return commandHelp.toArray(new String[0]);
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
