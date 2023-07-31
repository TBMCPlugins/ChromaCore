package buttondevteam.lib.chat

import buttondevteam.core.MainPlugin
import buttondevteam.lib.ChromaUtils
import buttondevteam.lib.chat.commands.*
import buttondevteam.lib.chat.commands.CommandUtils.coreCommand
import buttondevteam.lib.chat.commands.CommandUtils.coreCommandNoOp
import buttondevteam.lib.chat.commands.CommandUtils.coreExecutable
import buttondevteam.lib.chat.commands.CommandUtils.getDefaultForEasilyRepresentable
import buttondevteam.lib.chat.commands.CommandUtils.isCommand
import buttondevteam.lib.chat.commands.CommandUtils.isEasilyRepresentable
import buttondevteam.lib.chat.commands.CommandUtils.subcommandData
import buttondevteam.lib.chat.commands.CommandUtils.subcommandDataNoOp
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

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

    /**
     * Handle the given command line as sent by the sender.
     *
     * @param sender The sender who sent the command
     * @param commandline The command line, including the leading command char
     */
    open fun handleCommand(sender: TP, commandline: String): Boolean {
        val results = dispatcher.parse(commandline.removePrefix("/"), sender)
        if (results.reader.canRead()) {
            return if (results.context.nodes.isNotEmpty()) {
                for ((node, ex) in results.exceptions) {
                    sender.sendMessage("${ChatColor.RED}${ex.message}")
                    executeHelpText(results.context.build(results.reader.string))
                }
                true
            } else {
                false // Unknown command
            }
        }
        val executeCommand: () -> Unit = {
            try {
                dispatcher.execute(results)
            } catch (e: CommandSyntaxException) {
                sender.sendMessage(e.message)
            } catch (e: Exception) {
                ChromaUtils.throwWhenTested(e, "Command execution failed for sender ${sender.name}(${sender.javaClass.canonicalName}) and message $commandline")
            }
        }
        if (ChromaUtils.isTest) {
            executeCommand()
        } else {
            //Needed because permission checking may load the (perhaps offline) sender's file which is disallowed on the main thread
            Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.instance, executeCommand)
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
            assert(fullPath.isNotBlank()) { "No path found for command class ${command.javaClass.name} and method ${meth.name}" }
            val (lastNode, mainNodeMaybe, remainingPath) = registerNodeFromPath(fullPath)
            val execNode = getExecutableNode(meth, command, ann, remainingPath, CommandArgumentHelpManager(command), fullPath)
            lastNode.addChild(execNode)
            val mainNode = mainNodeMaybe ?: execNode
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
    private fun getExecutableNode(
        method: Method, command: TC, ann: Subcommand, remainingPath: String,
        argHelpManager: CommandArgumentHelpManager<TC, TP>, fullPath: String
    ): CoreExecutableNode<TP, TC> {
        val (params, senderType) = getCommandParametersAndSender(method, argHelpManager) // Param order is important
        val paramMap = HashMap<String, CommandArgument>()
        for (param in params) {
            paramMap[param.name] = param
        }
        val helpText = command.getHelpText(method, ann)
        val node = CoreCommandBuilder.literal(
            remainingPath, senderType, paramMap, params, command,
            { helpText }, // TODO: Help text getter support
            { sender: TP, data: SubcommandData<TC, TP> -> hasPermission(sender, data) },
            method.annotations.filterNot { it is Subcommand }.toTypedArray(),
            fullPath,
            method
        ).executes(this::executeHelpText)

        fun getArgNodes(parent: ArgumentBuilder<TP, *>, params: MutableList<CommandArgument>): Boolean {
            val param = params.removeFirst()
            val argType = getArgumentType(param)
            val arg = CoreArgumentBuilder.argument<TP, _>(param.name, argType, param.optional)
            if (params.isEmpty()) {
                arg.executes { context: CommandContext<TP> -> executeCommand(context) }
            } else {
                arg.executes(::executeHelpText)
            }
            if (params.isNotEmpty()) {
                if (getArgNodes(arg, params)) {
                    arg.executes { context: CommandContext<TP> -> executeCommand(context) }
                }
            }
            parent.then(arg)
            return param.optional
        }

        if (params.isEmpty() || getArgNodes(node, params.toMutableList())) {
            node.executes(::executeCommand)
        }
        return node.build().coreExecutable() ?: error("Command node should be executable but isn't: $fullPath")
    }

    /**
     * Registers all necessary no-op nodes for the given path.
     *
     * @param path The full command path
     * @return The last no-op node that can be used to register the executable node,
     * the main command node and the last part of the command path (that isn't registered yet)
     */
    private fun registerNodeFromPath(path: String): Triple<CommandNode<TP>, CoreCommandNode<TP, *>?, String> {
        val split = path.split(" ")
        var parent: CommandNode<TP> = dispatcher.root
        var mainCommand: CoreCommandNode<TP, *>? = null
        split.dropLast(1).forEachIndexed { i, part ->
            val child = parent.getChild(part)
            if (child == null) parent.addChild(CoreCommandBuilder.literalNoOp<TP, TC>(part) { emptyArray() }
                .executes(::executeHelpText).build().also { parent = it })
            else parent = child
            if (i == 0) mainCommand =
                parent as CoreCommandNode<TP, *> // Has to be our own literal node, if not, well, error
        }
        return Triple(parent, mainCommand, split.last())
    }

    fun getCommandList(sender: TP): Array<String> {
        return commandNodes.filter { it.data.hasPermission(sender) }
            .map { commandChar + it.data.fullPath }.toTypedArray()
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
        val usage = argHelpManager.getParameterHelpForMethod(method)?.ifEmpty { null }
        val paramNames = usage?.split(" ")
        return Pair(
            parameters.drop(1).zip(paramNames ?: (1 until parameters.size).map { i -> "param$i" })
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
                        name, param.annotations.filterNot { it is OptionalArg || it is NumberArg || it is TextArg }.toTypedArray()
                    )
                }, parameters[0].type
        )
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
        else if (ptype == Int::class.javaPrimitiveType || ptype == Int::class.javaObjectType
            || ptype == Byte::class.javaPrimitiveType || ptype == Byte::class.javaObjectType
            || ptype == Short::class.javaPrimitiveType || ptype == Short::class.javaObjectType
        )
            IntegerArgumentType.integer(lowerLimit.toInt(), upperLimit.toInt())
        else if (ptype == Long::class.javaPrimitiveType || ptype == Long::class.javaObjectType)
            LongArgumentType.longArg(lowerLimit.toLong(), upperLimit.toLong())
        else if (ptype == Float::class.javaPrimitiveType || ptype == Float::class.javaObjectType)
            FloatArgumentType.floatArg(lowerLimit.toFloat(), upperLimit.toFloat())
        else if (ptype == Double::class.javaPrimitiveType || ptype == Double::class.javaObjectType)
            DoubleArgumentType.doubleArg(lowerLimit, upperLimit)
        else if (ptype == Char::class.javaPrimitiveType || ptype == Char::class.javaObjectType)
            StringArgumentType.word()
        else if (ptype == Boolean::class.javaPrimitiveType || ptype == Boolean::class.javaObjectType)
            BoolArgumentType.bool()
        else StringArgumentType.word()
    }

    /**
     * Displays the help text based on the executed command. Each command node might have a help text stored.
     * The help text is displayed either because of incorrect usage or it's explicitly requested.
     *
     * @param context The command context
     * @return Vanilla command success level (0)
     */
    private fun executeHelpText(context: CommandContext<TP>): Int { // TODO: Add usage string automatically (and dynamically?)
        val node = context.nodes.lastOrNull()?.node ?: error("No nodes found when executing help text for ${context.input}!")
        val helpText = node.subcommandDataNoOp()?.getHelpText(context.source) ?: error("No subcommand data found when executing help text for ${context.input}")
        if (node.isCommand()) {
            val subs = getSubcommands(node.coreCommandNoOp()!!)
                .filter { it.data.hasPermission(context.source) }
                .map { commandChar + it.data.fullPath }.sorted()
            val messages = if (subs.isNotEmpty()) {
                helpText + "${ChatColor.GOLD}---- Subcommands ----" + subs
            } else {
                helpText
            }
            context.source.sendMessage(messages)
        }
        return 0
    }

    /**
     * Executes the command itself by calling the subcommand method associated with the input command node.
     *
     * @param context The command context
     * @return Vanilla command success level (0)
     */
    protected open fun executeCommand(context: CommandContext<TP>): Int {
        val sd = context.nodes.lastOrNull()?.node?.subcommandData<_, TC>() ?: error("Could not find suitable command node for command ${context.input}")
        val sender = context.source

        if (!sd.hasPermission(sender)) {
            sender.sendMessage("${ChatColor.RED}You don't have permission to use this command")
            return 1
        }
        // TODO: WIP

        val convertedSender = convertSenderType(sender, sd.senderType)
        if (convertedSender == null) {
            //TODO: Should have a prettier display of Command2 classes here
            val type = sd.senderType.simpleName.fold("") { s, ch -> s + if (ch.isUpperCase()) " " + ch.lowercase() else ch }.trim()
            sender.sendMessage("${ChatColor.RED}You need to be a $type to use this command.")
            executeHelpText(context) //Send what the command is about, could be useful for commands like /member where some subcommands aren't player-only
            return 0
        }

        val params = executeGetArguments(sd, context, sender) ?: return executeHelpText(context)

        // TODO: Varargs support? (colors?)
        // TODO: Character handling (strlen)

        executeInvokeCommand(sd, sender, convertedSender, params, context)
        return 0
    }

    private fun executeGetArguments(sd: SubcommandData<TC, TP>, context: CommandContext<TP>, sender: TP): MutableList<Any?>? {
        val params = mutableListOf<Any?>()
        for (argument in sd.argumentsInOrder) {
            try {
                if (argument.type.isEasilyRepresentable()) {
                    val userArgument = context.getArgument(argument.name, argument.type)
                    params.add(userArgument)
                } else {
                    val userArgument = context.getArgument(argument.name, String::class.java)
                    val converter = paramConverters[argument.type]
                        ?: error("No suitable converter found for ${argument.type} ${argument.name}")
                    val result = converter.converter.apply(userArgument)
                    if (result == null) {
                        sender.sendMessage("${ChatColor.RED}Error: ${converter.errormsg}")
                        return null
                    }
                    params.add(result)
                }
            } catch (e: IllegalArgumentException) {
                if (ChromaUtils.isTest && e.message?.contains("No such argument '${argument.name}' exists on this command") != true) {
                    println("For command ${sd.fullPath}:")
                    e.printStackTrace()
                }
                if (argument.optional) {
                    params.add(argument.type.getDefaultForEasilyRepresentable())
                } else {
                    return null
                }
            }
        }
        return params
    }

    /**
     * Invokes the command method with the given sender and parameters.
     */
    private fun executeInvokeCommand(sd: SubcommandData<TC, TP>, sender: TP, actualSender: Any, params: List<Any?>, context: CommandContext<TP>) {
        val invokeCommand = {
            try {
                val ret = sd.executeCommand(actualSender, *params.toTypedArray())
                if (ret is Boolean) {
                    if (!ret) //Show usage
                        executeHelpText(context)
                } else if (ret != null)
                    throw Exception("Wrong return type! Must return a boolean or void. Return value: $ret")
            } catch (e: InvocationTargetException) {
                ChromaUtils.throwWhenTested(e.cause ?: e, "An error occurred in a command handler for ${sd.fullPath}!")
            } catch (e: Exception) {
                ChromaUtils.throwWhenTested(e, "Command handling failed for sender $sender and subcommand ${sd.fullPath}")
            }
        }
        if (runOnPrimaryThread && !ChromaUtils.isTest)
            Bukkit.getScheduler().runTask(MainPlugin.instance, invokeCommand)
        else
            invokeCommand()
    }

    abstract fun hasPermission(sender: TP, data: SubcommandData<TC, TP>): Boolean

    /**
     * Get all registered command nodes. This returns all registered Chroma commands with all the information about them.
     *
     * @return A set of command node objects containing the commands
     */
    val commandNodes: Set<CoreExecutableNode<TP, TC>>
        get() = getSubcommands(true, dispatcher.root).toSet()

    /**
     * Get a node that belongs to the given command.
     *
     * @param command The exact name of the command
     * @return A command node
     */
    fun getCommandNode(command: String): CoreCommandNode<TP, NoOpSubcommandData>? { // TODO: What should this return? No-op? Executable? What's the use case?
        return dispatcher.root.getChild(command)?.coreCommand()
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
        unregisterCommandIf(condition, dispatcher.root, nested)
    }

    private fun unregisterCommandIf(
        condition: Predicate<CoreCommandNode<TP, SubcommandData<TC, TP>>>,
        root: CommandNode<TP>,
        nested: Boolean
    ) {
        // Can't use getCoreChildren() here because the collection needs to be modifiable
        if (nested) for (child in root.children)
            child.coreCommand<_, NoOpSubcommandData>()?.let { unregisterCommandIf(condition, it, true) }
        root.children.removeIf { node ->
            node.coreExecutable<TP, TC>()
                ?.let { condition.test(it) }
                ?: node.children.isEmpty()
        }
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
        return mainCommand.coreCommand<_, NoOpSubcommandData>()?.let { getSubcommands(deep, it) } ?: emptyList()
    }

    private fun getSubcommands(
        deep: Boolean = true,
        root: CommandNode<TP>
    ): List<CoreExecutableNode<TP, TC>> {
        return root.children.mapNotNull { it.coreExecutable<TP, TC>() } +
            if (deep) root.children.flatMap { child -> child.coreCommand<_, NoOpSubcommandData>()?.let { getSubcommands(deep, it) } ?: emptyList() } else emptyList()
    }
}