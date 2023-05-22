package buttondevteam.lib.chat

import buttondevteam.core.MainPlugin
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.chat.commands.CommandUtils
import buttondevteam.lib.chat.commands.CommandUtils.coreArgument
import buttondevteam.lib.chat.commands.CommandUtils.coreExecutable
import buttondevteam.lib.chat.commands.MCCommandSettings
import buttondevteam.lib.chat.commands.SubcommandData
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayerBase
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import me.lucko.commodore.Commodore
import me.lucko.commodore.CommodoreProvider
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.*
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class Command2MC : Command2<ICommand2MC, Command2MCSender>('/', true), Listener {
    /**
     * Don't use directly, use the method in Component and ButtonPlugin to automatically unregister the command when needed.
     *
     * @param command The command to register
     */
    override fun registerCommand(command: ICommand2MC) {
        val commandNode = super.registerCommandSuper(command)
        val bcmd = registerOfficially(command, commandNode)
        if (bcmd != null) // TODO: Support aliases
            super.registerCommandSuper(command)
        val permPrefix = "chroma.command."
        //Allow commands by default, it will check mod-only
        val nodes = commandNode.coreExecutable<Command2MCSender, ICommand2MC>()
            ?.let { getSubcommands(commandNode) + it } ?: getSubcommands(commandNode)
        for (node in nodes) {
            val subperm = permPrefix + node.data.fullPath.replace(' ', '.')
            if (Bukkit.getPluginManager().getPermission(subperm) == null) //Check needed for plugin reset
                Bukkit.getPluginManager().addPermission(Permission(subperm, PermissionDefault.TRUE))
            val pg = permGroup(node.data)
            if (pg.isEmpty()) continue
            val permGroup = "chroma.$pg"
            //Do not allow any commands that belong to a group by default
            if (Bukkit.getPluginManager().getPermission(permGroup) == null) //It may occur multiple times
                Bukkit.getPluginManager().addPermission(Permission(permGroup, PermissionDefault.OP))
        }
    }

    override fun hasPermission(sender: Command2MCSender, data: SubcommandData<ICommand2MC, Command2MCSender>): Boolean {
        if (sender.sender.isConsole) return true //Always allow the console

        var p = true
        val cmdperm = "chroma.command.${data.fullPath.replace(' ', '.')}"
        // TODO: Register a permission for the main command as well - the previous implementation relied on the way the commands were defined
        val perms = arrayOf(
            cmdperm,
            permGroup(data).let { if (it.isEmpty()) null else "chroma.$it" }
        )
        for (perm in perms) {
            if (perm != null) {
                if (p) { //Use OfflinePlayer to avoid fetching player data
                    p = MainPlugin.permission.playerHas(
                        sender.sender.player?.location?.world?.name,
                        sender.sender.offlinePlayer,
                        perm
                    )
                } else break //If any of the permissions aren't granted then don't allow
            }
        }
        return p
    }

    /**
     * Returns the first group found in the hierarchy starting from the command method **or** the mod group if *any* of the superclasses are mod only.
     *
     * @param data The data of the subcommand to check
     * @return The permission group for the subcommand or empty string
     */
    private fun permGroup(data: SubcommandData<ICommand2MC, Command2MCSender>): String {
        val group = data.annotations.filterIsInstance<MCCommandSettings>().map {
            if (it.permGroup.isEmpty() && it.modOnly) MCCommandSettings.MOD_GROUP else ""
        }.firstOrNull()
        return group ?: ""
    }

    /**
     * Automatically colors the message red.
     * {@see super#addParamConverter}
     */
    override fun <T> addParamConverter(
        cl: Class<T>,
        converter: Function<String, T?>,
        errormsg: String,
        allSupplier: Supplier<Iterable<String>>
    ) {
        super.addParamConverter(cl, converter, "${ChatColor.RED}$errormsg", allSupplier)
    }

    override fun convertSenderType(sender: Command2MCSender, senderType: Class<*>): Any? {
        val original = super.convertSenderType(sender, senderType)
        if (original != null) {
            return original
        }
        // Check Bukkit sender type - TODO: This is no longer the Bukkit sender type
        if (senderType.isAssignableFrom(sender.sender.javaClass))
            return sender.sender
        //The command expects a user of our system
        if (ChromaGamerBase::class.java.isAssignableFrom(senderType)) {
            val cg = sender.sender
            if (cg.javaClass == senderType)
                return cg
        }
        return null
    }

    fun unregisterCommands(plugin: ButtonPlugin) {
        unregisterCommandIf({ node -> Optional.ofNullable(node.data.command).map { obj -> obj.plugin }.map { obj -> plugin == obj }.orElse(false) }, true)
    }

    fun unregisterCommands(component: Component<*>) {
        unregisterCommandIf({ node ->
            Optional.ofNullable(node.data.command).map { obj: ICommand2MC -> obj.plugin }
                .map { comp: ButtonPlugin -> component.javaClass.simpleName == comp.javaClass.simpleName }.orElse(false)
        }, true)
    }

    override fun handleCommand(sender: Command2MCSender, commandline: String): Boolean {
        return handleCommand(sender, commandline, true)
    }

    private fun handleCommand(sender: Command2MCSender, commandline: String, checkPlugin: Boolean): Boolean {
        val i = commandline.indexOf(' ')
        val mainpath = commandline.substring(1, if (i == -1) commandline.length else i) //Without the slash
        //Our commands aren't PluginCommands, unless it's specified in the plugin.yml
        // So we need to handle the command if it's not a plugin command or if it's a plugin command, but for a ButtonPlugin
        return if (!checkPlugin || MainPlugin.instance.prioritizeCustomCommands.get()
            || Bukkit.getPluginCommand(mainpath)?.let { it.plugin is ButtonPlugin } != false
        )
            super.handleCommand(sender, commandline) else false
    }

    private var shouldRegisterOfficially = true
    private fun registerOfficially(command: ICommand2MC, node: CoreCommandNode<Command2MCSender, *>): Command? {
        return if (!shouldRegisterOfficially) null else try {
            val cmdmap =
                Bukkit.getServer().javaClass.getMethod("getCommandMap").invoke(Bukkit.getServer()) as SimpleCommandMap
            val path = command.commandPath
            val x = path.indexOf(' ')
            val mainPath = path.substring(0, if (x == -1) path.length else x)
            val bukkitCommand: Command
            //Commands conflicting with Essentials have to be registered in plugin.yml
            //The label with the fallback prefix is always registered
            val oldcmd = cmdmap.getCommand("${command.plugin.name}:$mainPath")
            if (oldcmd == null) {
                bukkitCommand = BukkitCommand(mainPath)
                cmdmap.register(command.plugin.name, bukkitCommand)
            } else {
                bukkitCommand = oldcmd
                if (bukkitCommand is PluginCommand) bukkitCommand.setExecutor(this::executeCommand)
            }
            TabcompleteHelper.registerTabcomplete(command, node, bukkitCommand)
            bukkitCommand
        } catch (e: Exception) {
            val component = command.component
            if (component == null)
                TBMCCoreAPI.SendException("Failed to register command in command map!", e, command.plugin)
            else
                TBMCCoreAPI.SendException("Failed to register command in command map!", e, component)
            shouldRegisterOfficially = false
            null
        }
    }

    private fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val user = ChromaGamerBase.getFromSender(sender) // TODO: Senders should only be used for TBMCPlayerBase classes.
        ///trim(): remove space if there are no args
        handleCommand(
            Command2MCSender(user as TBMCPlayerBase, user.channel.get(), sender),
            ("/${command.name} ${args.joinToString(" ")}").trim { it <= ' ' }, false
        )
        return true
    }

    private class BukkitCommand(name: String) : Command(name) {
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
            return ButtonPlugin.command2MC.executeCommand(sender, this, commandLabel, args)
        }

        @Throws(IllegalArgumentException::class)
        override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
            return emptyList()
        }

        @Throws(IllegalArgumentException::class)
        override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>?, location: Location?): MutableList<String> {
            return mutableListOf()
        }
    }

    private object TabcompleteHelper {
        private val commodore: Commodore by lazy {
            val commodore = CommodoreProvider.getCommodore(MainPlugin.instance) //Register all to the Core, it's easier
            commodore.register(
                literal<Any?>("un") // TODO: This is a test
                    .redirect(argument<Any?, String>("unsomething", StringArgumentType.word())
                        .suggests { _, builder -> builder.suggest("untest").buildFuture() }.build()
                    )
            )
            commodore
        }

        fun registerTabcomplete(command2MC: ICommand2MC, commandNode: CoreCommandNode<Command2MCSender, *>, bukkitCommand: Command) {
            if (!CommodoreProvider.isSupported()) {
                throw UnsupportedOperationException("Commodore is not supported! Please use 1.14 or higher. Server version: ${Bukkit.getVersion()}")
            }
            // TODO: Allow extending annotation processing for methods and parameters
            val customTabCompleteMethods = command2MC.javaClass.declaredMethods
                .flatMap { method ->
                    method.getAnnotation(CustomTabCompleteMethod::class.java)?.let { ctcmAnn ->
                        (ctcmAnn.subcommand.takeIf { it.isNotEmpty() }
                            ?: arrayOf(CommandUtils.getCommandPath(method.name, ' ').trim { it <= ' ' }))
                            .map { name -> Triple(name, ctcmAnn, method) }
                    } ?: emptyList()
                }
            val mcNode = CommandUtils.mapSubcommands(commandNode) { node ->
                val builder = node.createBuilder()
                val argNode = node.coreArgument() ?: return@mapSubcommands builder
                val subpath = "" // TODO: This needs the same processing as the command path to have the same flexibility
                val argData = argNode.commandData.arguments[argNode.name] ?: return@mapSubcommands builder
                val customTCTexts = argData.annotations.filterIsInstance<CustomTabComplete>().flatMap { it.value.asList() }
                val customTCmethod = customTabCompleteMethods.firstOrNull { (name, ann, _) ->
                    name == subpath && argData.name.replace("[\\[\\]<>]".toRegex(), "") == ann.param
                }
                (builder as RequiredArgumentBuilder<Command2MCSender, *>).suggests { context, b ->
                    val sbuilder = if (argData.greedy) { //Do it before the builder is used
                        val nextTokenStart = context.input.lastIndexOf(' ') + 1
                        b.createOffset(nextTokenStart)
                    } else b
                    // Suggest custom tab complete texts
                    for (ctc in customTCTexts) {
                        sbuilder.suggest(ctc)
                    }
                    val ignoreCustomParamType = false // TODO: This should be set by the @CustomTabCompleteMethod annotation
                    // TODO: Custom tab complete method handling
                    if (!ignoreCustomParamType) {
                        val converter = getParamConverter(argData.type, command2MC)
                        if (converter != null) {
                            val suggestions = converter.allSupplier.get()
                            for (suggestion in suggestions) sbuilder.suggest(suggestion)
                        }
                    }
                    if (argData.type === Boolean::class.javaPrimitiveType || argData.type === Boolean::class.java)
                        sbuilder.suggest("true").suggest("false")
                    val loweredInput = sbuilder.remaining.lowercase(Locale.getDefault())
                    // The list is automatically ordered, so we need to put the <param> at the end after that
                    // We're also removing all suggestions that don't start with the input
                    sbuilder.suggest(argData.name).buildFuture().whenComplete { ss, _ ->
                        ss.list.add(ss.list.removeAt(0))
                    }.whenComplete { ss, _ ->
                        ss.list.removeIf { s ->
                            s.text.lowercase().let { !it.startsWith("<") && !it.startsWith("[") && !it.startsWith(loweredInput) }
                        }
                    }
                }
                builder
            }

            commodore.register(mcNode as LiteralCommandNode<*>)
            commodore.register(literal<Command2MCSender>("${command2MC.plugin.name.lowercase()}:${mcNode.name}").redirect(mcNode))
            for (alias in bukkitCommand.aliases) {
                commodore.register(literal<Command2MCSender>(alias).redirect(mcNode))
                commodore.register(literal<Command2MCSender>("${command2MC.plugin.name.lowercase()}:${alias}").redirect(mcNode))
            }
        }
    }

    companion object {
        private fun getParamConverter(cl: Class<*>, command2MC: ICommand2MC): ParamConverter<*>? {
            val converter = ButtonPlugin.command2MC.paramConverters[cl]
            if (converter == null) {
                val msg = "Could not find a suitable converter for type " + cl.simpleName
                val exception: Exception = NullPointerException("converter is null")
                val component = command2MC.component
                if (component == null) TBMCCoreAPI.SendException(msg, exception, command2MC.plugin)
                else TBMCCoreAPI.SendException(msg, exception, component)
                return null
            }
            return converter
        }
    }
}

private typealias CNode = CommandNode<Command2MCSender>
