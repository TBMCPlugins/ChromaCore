package buttondevteam.lib.chat

import buttondevteam.core.MainPlugin
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.chat.commands.*
import buttondevteam.lib.chat.commands.CommandUtils.core
import buttondevteam.lib.chat.commands.CommandUtils.coreExecutable
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import org.bukkit.Bukkit
import java.lang.reflect.Method
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Collectors

/**
 * The method name is the subcommand, use underlines (_) to add further subcommands.
 * The args may be null if the conversion failed and it's optional.
 */
abstract class Command2<TC : ICommand2<TP>, TP : Command2Sender>(
    /**
     * The first character in the command line that shows that it's a command.
     */
    private val commandChar: Char,

    /**
     * Whether the command's actual code has to be run on the primary thread.
     */
    private val runOnPrimaryThread: Boolean
) {
    /**
     * Parameters annotated with this receive all the remaining arguments
     */
    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class TextArg

    /**
     * Methods annotated with this will be recognised as subcommands
     */
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Subcommand(
        /**
         * Help text to show players. A usage message will be also shown below it.
         */
        val helpText: Array<String> = [],
        /**
         * Aliases for the subcommand that can be used to invoke it in addition to the method name.
         */
        val aliases: Array<String> = [] // TODO
    ) {
    }

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class OptionalArg

    protected class ParamConverter<T>(
        val converter: Function<String, T?>,
        val errormsg: String,
        val allSupplier: Supplier<Iterable<String>>
    )

    protected val paramConverters = HashMap<Class<*>, ParamConverter<*>>()
    private val commandHelp = ArrayList<String>() //Mainly needed by Discord
    private val dispatcher = CommandDispatcher<TP>()

    /**
     * Adds a param converter that obtains a specific object from a string parameter.
     * The converter may return null to signal an error.
     *
     * @param <T>         The type of the result
     * @param cl          The class of the result object
     * @param converter   The converter to use
     * @param allSupplier The supplier of all possible values (ideally)
    </T> */
    open fun <T> addParamConverter(
        cl: Class<T>,
        converter: Function<String, T?>,
        errormsg: String,
        allSupplier: Supplier<Iterable<String>>
    ) {
        paramConverters[cl] = ParamConverter(converter, errormsg, allSupplier)
    }

    open fun handleCommand(sender: TP, commandline: String): Boolean {
        val results = dispatcher.parse(commandline, sender)
        if (results.reader.canRead()) {
            return false // Unknown command
        }
        //Needed because permission checking may load the (perhaps offline) sender's file which is disallowed on the main thread
        Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.instance) { _ ->
            try {
                dispatcher.execute(results)
            } catch (e: CommandSyntaxException) {
                sender.sendMessage(e.message)
            } catch (e: Exception) {
                TBMCCoreAPI.SendException(
                    "Command execution failed for sender " + sender.name + "(" + sender.javaClass.canonicalName + ") and message " + commandline,
                    e,
                    MainPlugin.instance
                )
            }
        }
        return true //We found a method
    }

    //TODO: Add to the help
    open fun convertSenderType(sender: TP, senderType: Class<*>): Any? {
        //The command either expects a CommandSender or it is a Player, or some other expected type
        if (senderType.isAssignableFrom(sender.javaClass)) return sender
        return null
    }

    /**
     * Register a command in the command system. The way this command gets registered may change depending on the implementation.
     * Always invoke [.registerCommandSuper] when implementing this method.
     *
     * @param command The command to register
     */
    abstract fun registerCommand(command: TC)

    /**
     * Registers a command in the Command2 system, so it can be looked up and executed.
     *
     * @param command The command to register
     * @return The Brigadier command node if you need it for something (like tab completion)
     */
    protected fun registerCommandSuper(command: TC): CoreCommandNode<TP, *> {
        var mainCommandNode: CoreCommandNode<TP, *>? = null
        for (meth in command.javaClass.methods) {
            val ann = meth.getAnnotation(Subcommand::class.java) ?: continue
            val fullPath = command.commandPath + CommandUtils.getCommandPath(meth.name, ' ')
            val (lastNode, mainNode, remainingPath) = registerNodeFromPath(fullPath)
            lastNode.addChild(getExecutableNode(meth, command, ann, remainingPath, CommandArgumentHelpManager(command), fullPath))
            if (mainCommandNode == null) mainCommandNode = mainNode
            else if (mainNode.name != mainCommandNode.name) {
                MainPlugin.instance.logger.warning("Multiple commands are defined in the same class! This is not supported. Class: " + command.javaClass.simpleName)
            }
        }
        if (mainCommandNode == null) {
            throw RuntimeException("There are no subcommands defined in the command class " + command.javaClass.simpleName + "!")
        }
        return mainCommandNode
    }

    /**
     * Returns the node that can actually execute the given subcommand.
     *
     * @param method The subcommand method
     * @param command The command object
     * @param ann The subcommand annotation
     * @param remainingPath The command path
     * @param argHelpManager The object that gets the usage text from code
     * @param fullPath The full command path as registered
     * @return The executable node
     */
    private fun getExecutableNode(method: Method, command: TC, ann: Subcommand, remainingPath: String,
                                  argHelpManager: CommandArgumentHelpManager<TC, TP>, fullPath: String): LiteralCommandNode<TP> {
        val (params, _) = getCommandParametersAndSender(method, argHelpManager) // Param order is important
        val paramMap = HashMap<String, CommandArgument>()
        for (param in params) {
            paramMap[param.name] = param
        }
        val helpText = command.getHelpText(method, ann)
        val node = CoreCommandBuilder.literal(
            remainingPath, params[0].type, paramMap, params, command,
            { helpText }, // TODO: Help text getter support
            { sender: TP, data: SubcommandData<TC, TP> -> hasPermission(sender, data) },
            method.annotations.filterNot { it is Subcommand }.toTypedArray(),
            fullPath
        )
            .executes { context: CommandContext<TP> -> executeCommand(context) }
        var parent: ArgumentBuilder<TP, *> = node
        for (param in params) { // Register parameters in the right order
            val argType = getArgumentType(param)
            parent.then(CoreArgumentBuilder.argument<TP, _>(param.name, argType, param.optional).also { parent = it })
        }
        return node.build()
    }

    /**
     * Registers all necessary no-op nodes for the given path.
     *
     * @param path The full command path
     * @return The last no-op node that can be used to register the executable node,
     * the main command node and the last part of the command path (that isn't registered yet)
     */
    private fun registerNodeFromPath(path: String): Triple<CommandNode<TP>, CoreCommandNode<TP, *>, String> {
        val split = path.split(" ")
        var parent: CommandNode<TP> = dispatcher.root
        var mainCommand: CoreCommandNode<TP, *>? = null
        split.forEachIndexed { i, part ->
            val child = parent.getChild(part)
            if (child == null) parent.addChild(CoreCommandBuilder.literalNoOp<TP, TC>(part, getSubcommandList())
                .executes(::executeHelpText).build().also { parent = it })
            else parent = child
            if (i == 0) mainCommand =
                parent as CoreCommandNode<TP, *> // Has to be our own literal node, if not, well, error
        }
        return Triple(parent, mainCommand!!, split.last())
    }

    private fun getSubcommandList(): (Any) -> Array<String> {
        return {
            arrayOf("TODO") // TODO: Subcommand list
        }
    }

    /**
     * Get parameter data for the given subcommand. Attempts to read it from the commands file, if it fails, it will return generic info.
     * The first parameter is always the sender both in the methods themselves and in the returned array.
     *
     * @param method The method the subcommand is created from
     * @return Parameter data objects and the sender type
     * @throws RuntimeException If there is no sender parameter declared in the method
     */
    private fun getCommandParametersAndSender(
        method: Method,
        argHelpManager: CommandArgumentHelpManager<TC, TP>
    ): Pair<List<CommandArgument>, Class<*>> {
        val parameters = method.parameters
        if (parameters.isEmpty()) throw RuntimeException("No sender parameter for method '$method'")
        val usage = argHelpManager.getParameterHelpForMethod(method)
        val paramNames = usage?.split(" ")
        return Pair(
            parameters.zip(paramNames ?: (1 until parameters.size).map { i -> "param$i" })
                .map { (param, name) ->
                    val numAnn = param.getAnnotation(NumberArg::class.java)
                    CommandArgument(
                        name, param.type,
                        param.isVarArgs || param.isAnnotationPresent(TextArg::class.java),
                        if (numAnn == null) Pair(Double.MIN_VALUE, Double.MAX_VALUE) else Pair(
                            numAnn.lowerLimit,
                            numAnn.upperLimit
                        ),
                        param.isAnnotationPresent(OptionalArg::class.java),
                        name, param.annotations.filterNot { it is OptionalArg || it is NumberArg || it is TextArg }.toTypedArray())
                }, parameters[0].type)
    }

    /**
     * Converts the Chroma representation of the argument declaration into Brigadier format.
     * It does part of the command argument type processing.
     *
     * @param arg Our representation of the command argument
     * @return The Brigadier representation of the command argument
     */
    private fun getArgumentType(arg: CommandArgument?): ArgumentType<out Any> {
        val ptype = arg!!.type
        val (lowerLimit, upperLimit) = arg.limits
        return if (arg.greedy) StringArgumentType.greedyString()
        else if (ptype == String::class.java) StringArgumentType.word()
        else if (ptype == Int::class.javaPrimitiveType || ptype == Int::class.java
            || ptype == Byte::class.javaPrimitiveType || ptype == Byte::class.java
            || ptype == Short::class.javaPrimitiveType || ptype == Short::class.java)
            IntegerArgumentType.integer(lowerLimit.toInt(), upperLimit.toInt())
        else if (ptype == Long::class.javaPrimitiveType || ptype == Long::class.java)
            LongArgumentType.longArg(lowerLimit.toLong(), upperLimit.toLong())
        else if (ptype == Float::class.javaPrimitiveType || ptype == Float::class.java)
            FloatArgumentType.floatArg(lowerLimit.toFloat(), upperLimit.toFloat())
        else if (ptype == Double::class.javaPrimitiveType || ptype == Double::class.java)
            DoubleArgumentType.doubleArg(lowerLimit, upperLimit)
        else if (ptype == Char::class.javaPrimitiveType || ptype == Char::class.java) StringArgumentType.word()
        else if (ptype == Boolean::class.javaPrimitiveType || ptype == Boolean::class.java) BoolArgumentType.bool()
        else StringArgumentType.word()
    }

    /**
     * Displays the help text based on the executed command. Each command node might have a help text stored.
     * The help text is displayed either because of incorrect usage or it's explicitly requested.
     *
     * @param context The command context
     * @return Vanilla command success level (0)
     */
    private fun executeHelpText(context: CommandContext<TP>): Int {
        println("""
    Nodes:
    ${context.nodes.stream().map { node -> node.node.name + "@" + node.range }.collect(Collectors.joining("\n"))}
    """.trimIndent())
        return 0
    }

    /**
     * Executes the command itself by calling the subcommand method associated with the input command node.
     *
     * @param context The command context
     * @return Vanilla command success level (0)
     */
    private fun executeCommand(context: CommandContext<TP>): Int {
        println("Execute command")
        println("Should be running sync: $runOnPrimaryThread")

        /*if (!hasPermission(sender, sd.command, sd.method)) {
			sender.sendMessage("${ChatColor.RED}You don't have permission to use this command");
			return;
		}
		// TODO: WIP

            val type = sendertype.simpleName.fold("") { s, ch -> s + if (ch.isUpperCase()) " " + ch.lowercase() else ch }
            sender.sendMessage("${ChatColor.RED}You need to be a $type to use this command.")
            sender.sendMessage(sd.getHelpText(sender)) //Send what the command is about, could be useful for commands like /member where some subcommands aren't player-only

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
			invokeCommand.run();*/return 0
    }

    abstract fun hasPermission(sender: TP, data: SubcommandData<TC, TP>): Boolean
    val commandsText: Array<String> get() = commandHelp.toTypedArray()

    /**
     * Get all registered command nodes. This returns all registered Chroma commands with all the information about them.
     *
     * @return A set of command node objects containing the commands
     */
    val commandNodes: Set<CoreCommandNode<TP, NoOpSubcommandData>>
        get() = dispatcher.root.children.stream()
            .map { node: CommandNode<TP> -> node.core<TP, NoOpSubcommandData>() }
            .collect(Collectors.toUnmodifiableSet())

    /**
     * Get a node that belongs to the given command.
     *
     * @param command The exact name of the command
     * @return A command node
     */
    fun getCommandNode(command: String): CoreCommandNode<TP, NoOpSubcommandData>? { // TODO: What should this return? No-op? Executable? What's the use case?
        return dispatcher.root.getChild(command)?.core()
    }

    /**
     * Unregister all subcommands that were registered with the given command class.
     *
     * @param command The command class (object) to unregister
     */
    fun unregisterCommand(command: ICommand2<TP>) {
        dispatcher.root.children.removeIf { node: CommandNode<TP> -> node.coreExecutable<TP, TC>()?.data?.command === command }
    }

    /**
     * Unregisters all commands that match the given predicate.
     *
     * @param condition The condition for removing a given command
     */
    fun unregisterCommandIf(condition: Predicate<CoreCommandNode<TP, SubcommandData<TC, TP>>>, nested: Boolean) {
        dispatcher.root.children.removeIf { node -> node.coreExecutable<TP, TC>()?.let { condition.test(it) } ?: false }
        if (nested) for (child in dispatcher.root.children) unregisterCommandIf(condition, child.core())
    }

    private fun unregisterCommandIf(
        condition: Predicate<CoreCommandNode<TP, SubcommandData<TC, TP>>>,
        root: CoreCommandNode<TP, NoOpSubcommandData>
    ) {
        // TODO: Remvoe no-op nodes without children
        // Can't use getCoreChildren() here because the collection needs to be modifiable
        root.children.removeIf { node -> node.coreExecutable<TP, TC>()?.let { condition.test(it) } ?: false }
        for (child in root.children) unregisterCommandIf(condition, child.core())
    }

    /**
     * Get all subcommands of the specified command. Only returns executable nodes.
     *
     * @param mainCommand The command to get the subcommands of
     * @param deep Whether to get all subcommands recursively or only the direct children
     */
    fun getSubcommands(
        mainCommand: LiteralCommandNode<TP>,
        deep: Boolean = true
    ): List<CoreExecutableNode<TP, TC>> {
        return getSubcommands(deep, mainCommand.core())
    }

    private fun getSubcommands(
        deep: Boolean = true,
        root: CoreNoOpNode<TP>
    ): List<CoreExecutableNode<TP, TC>> {
        return root.children.mapNotNull { it.coreExecutable<TP, TC>() } +
            if (deep) root.children.flatMap { getSubcommands(deep, it.core()) } else emptyList()
    }
}