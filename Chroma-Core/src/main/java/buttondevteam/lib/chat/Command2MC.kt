package buttondevteam.lib.chat

import buttondevteam.core.MainPlugin
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.chat.commands.CommandUtils
import buttondevteam.lib.chat.commands.MCCommandSettings
import buttondevteam.lib.chat.commands.SubcommandData
import buttondevteam.lib.player.ChromaGamerBase
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import me.lucko.commodore.Commodore
import me.lucko.commodore.CommodoreProvider
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.command.*
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Function
import java.util.function.Supplier

class Command2MC : Command2<ICommand2MC, Command2MCSender>('/', true), Listener {
    /**
     * Don't use directly, use the method in Component and ButtonPlugin to automatically unregister the command when needed.
     *
     * @param command The command to register
     */
    override fun registerCommand(command: ICommand2MC) {
        /*String mainpath;
		var plugin = command.getPlugin();
		{
			String cpath = command.getCommandPath();
			int i = cpath.indexOf(' ');
			mainpath = cpath.substring(0, i == -1 ? cpath.length() : i);
		}*/
        val commandNode = super.registerCommandSuper(command)
        val bcmd = registerOfficially(command, commandNode)
        if (bcmd != null) // TODO: Support aliases
            super.registerCommandSuper(command)
        val perm = "chroma.command." + command.commandPath.replace(' ', '.')
        if (Bukkit.getPluginManager().getPermission(perm) == null) //Check needed for plugin reset
            Bukkit.getPluginManager().addPermission(Permission(perm,
                PermissionDefault.TRUE)) //Allow commands by default, it will check mod-only
        for (node in getSubcommands(commandNode)) {
            if (path.length > 0) {
                val subperm = perm + path
                if (Bukkit.getPluginManager().getPermission(subperm) == null) //Check needed for plugin reset
                    Bukkit.getPluginManager().addPermission(
                        Permission(
                            subperm,
                            PermissionDefault.TRUE
                        )
                    ) //Allow commands by default, it will check mod-only
            }
            val pg = permGroup(node.data)
            if (pg.isEmpty()) continue
            val permGroup = "chroma.$pg"
            if (Bukkit.getPluginManager().getPermission(permGroup) == null) //It may occur multiple times
                Bukkit.getPluginManager().addPermission(
                    Permission(
                        permGroup,
                        PermissionDefault.OP
                    )
                ) //Do not allow any commands that belong to a group
        }
    }

    override fun hasPermission(sender: Command2MCSender, data: SubcommandData<ICommand2MC, Command2MCSender>): Boolean {
        val mcsender = sender.sender
        if (mcsender is ConsoleCommandSender) return true //Always allow the console

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
                    p = if (mcsender is OfflinePlayer) MainPlugin.permission.playerHas(
                        if (mcsender is Player) mcsender.location.world?.name else null,
                        mcsender as OfflinePlayer,
                        perm
                    ) else false //Use sender's method
                    if (!p) p = mcsender.hasPermission(perm)
                } else break //If any of the permissions aren't granted then don't allow
            }
        }
        return p
    }

    /**
     * Returns the first group found in the hierarchy starting from the command method **or** the mod group if *any* of the superclasses are mod only.
     *
     * @param method The subcommand to check
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
        super.addParamConverter(cl, converter, "§c$errormsg", allSupplier)
    }

    override fun convertSenderType(sender: Command2MCSender, senderType: Class<*>): Any? {
        val original = super.convertSenderType(sender, senderType)
        if (original != null) {
            return original
        }
        // Check Bukkit sender type
        if (senderType.isAssignableFrom(sender.sender.javaClass))
            return sender.sender
        //The command expects a user of our system
        if (ChromaGamerBase::class.java.isAssignableFrom(senderType)) {
            val cg = ChromaGamerBase.getFromSender(sender.sender)
            if (cg?.javaClass == senderType)
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
        return if ((!checkPlugin || (MainPlugin.Instance.prioritizeCustomCommands.get() == true))
            || Bukkit.getPluginCommand(mainpath)?.let { it.plugin is ButtonPlugin } != false)
            super.handleCommand(sender, commandline) else false
    }

    private var shouldRegisterOfficially = true
    private fun registerOfficially(command: ICommand2MC, node: LiteralCommandNode<Command2MCSender>): Command? {
        return if (!shouldRegisterOfficially || command.plugin == null) null else try {
            val cmdmap = Bukkit.getServer().javaClass.getMethod("getCommandMap").invoke(Bukkit.getServer()) as SimpleCommandMap
            val path = command.commandPath
            val x = path.indexOf(' ')
            val mainPath = path.substring(0, if (x == -1) path.length else x)
            var bukkitCommand: Command
            run {
                //Commands conflicting with Essentials have to be registered in plugin.yml
                val oldcmd = cmdmap.getCommand(command.plugin.name + ":" + mainPath) //The label with the fallback prefix is always registered
                if (oldcmd == null) {
                    bukkitCommand = BukkitCommand(mainPath)
                    cmdmap.register(command.plugin.name, bukkitCommand)
                } else {
                    bukkitCommand = oldcmd
                    if (bukkitCommand is PluginCommand) (bukkitCommand as PluginCommand).executor = CommandExecutor { sender: CommandSender, command: Command, label: String, args: Array<String> -> this.executeCommand(sender, command, label, args) }
                }
                bukkitCommand = oldcmd ?: BukkitCommand(mainPath)
            }
            if (CommodoreProvider.isSupported()) TabcompleteHelper.registerTabcomplete(command, node, bukkitCommand)
            bukkitCommand
        } catch (e: Exception) {
            if (command.component == null) TBMCCoreAPI.SendException("Failed to register command in command map!", e, command.plugin) else TBMCCoreAPI.SendException("Failed to register command in command map!", e, command.component)
            shouldRegisterOfficially = false
            null
        }
    }

    private fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val user = ChromaGamerBase.getFromSender(sender)
        if (user == null) {
            TBMCCoreAPI.SendException("Failed to run Bukkit command for user!", Throwable("No Chroma user found"), MainPlugin.Instance)
            sender.sendMessage("§cAn internal error occurred.")
            return true
        }
        handleCommand(Command2MCSender(sender, user.channel.get(), sender),
            ("/" + command.name + " " + java.lang.String.join(" ", *args)).trim { it <= ' ' }, false) ///trim(): remove space if there are no args
        return true
    }

    private class BukkitCommand(name: String?) : Command(name) {
        override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
            return ButtonPlugin.command2MC.executeCommand(sender, this, commandLabel, args)
        }

        @Throws(IllegalArgumentException::class)
        override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
            return emptyList()
        }

        @Throws(IllegalArgumentException::class)
        override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>, location: Location): List<String> {
            return emptyList()
        }
    }

    private object TabcompleteHelper {
        private var commodore: Commodore? = null
        private fun appendSubcommand(path: String, parent: CommandNode<Any>,
                                     subcommand: SubcommandData<ICommand2MC>?): LiteralCommandNode<Any> {
            var scmd: LiteralCommandNode<Any>
            if (parent.getChild(path) as LiteralCommandNode<kotlin.Any?>?. also { scmd = it } != null) return scmd
            val scmdBuilder = LiteralArgumentBuilder.literal<Any>(path)
            if (subcommand != null) scmdBuilder.requires { o: Any? ->
                val sender = commodore!!.getBukkitSender(o)
                subcommand.hasPermission(sender)
            }
            scmd = scmdBuilder.build()
            parent.addChild(scmd)
            return scmd
        }

        private fun registerTabcomplete(command2MC: ICommand2MC, commandNode: LiteralCommandNode<Command2MCSender>, bukkitCommand: Command) {
            if (commodore == null) {
                commodore = CommodoreProvider.getCommodore(MainPlugin.Instance) //Register all to the Core, it's easier
                commodore.register(LiteralArgumentBuilder.literal<Any?>("un").redirect(RequiredArgumentBuilder.argument<Any?, String>("unsomething",
                    StringArgumentType.word()).suggests { context: CommandContext<Any?>?, builder: SuggestionsBuilder -> builder.suggest("untest").buildFuture() }.build()))
            }
            commodore!!.dispatcher.root.getChild(commandNode.name) // TODO: Probably unnecessary
            val customTCmethods = Arrays.stream(command2MC.javaClass.declaredMethods) //val doesn't recognize the type arguments
                .flatMap { method: Method ->
                    Optional.ofNullable(method.getAnnotation(CustomTabCompleteMethod::class.java)).stream()
                        .flatMap { ctcmAnn: CustomTabCompleteMethod ->
                            val paths = Optional.of<Array<String?>>(ctcmAnn.subcommand()).filter { s: Array<String?> -> s.size > 0 }
                                .orElseGet {
                                    arrayOf(
                                        CommandUtils.getCommandPath(method.name, ' ').trim { it <= ' ' }
                                    )
                                }
                            Arrays.stream(paths).map { name: String? -> Triplet(name, ctcmAnn, method) }
                        }
                }.toList()
            for (subcmd in subcmds) {
                val subpathAsOne = CommandUtils.getCommandPath(subcmd.method.getName(), ' ').trim { it <= ' ' }
                val subpath = subpathAsOne.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var scmd: CommandNode<Any> = cmd
                if (subpath[0].length > 0) { //If the method is def, it will contain one empty string
                    for (s in subpath) {
                        scmd = appendSubcommand(s, scmd, subcmd) //Add method name part of the path (could_be_multiple())
                    }
                }
                val parameters: Array<Parameter> = subcmd.method.getParameters()
                for (i in 1 until parameters.size) { //Skip sender
                    val parameter = parameters[i]
                    val customParamType: Boolean
                    // TODO: Arg type
                    val param: Any = subcmd.parameters.get(i - 1)
                    val customTC = Optional.ofNullable(parameter.getAnnotation(CustomTabComplete::class.java))
                        .map(Function<CustomTabComplete, Array<String>> { obj: CustomTabComplete -> obj.value() })
                    val customTCmethod = customTCmethods.stream().filter { t: Triplet<String?, CustomTabCompleteMethod, Method> -> subpathAsOne.equals(t.value0, ignoreCase = true) }
                        .filter { t: Triplet<String?, CustomTabCompleteMethod, Method> -> param.replaceAll("[\\[\\]<>]", "").equalsIgnoreCase(t.value1.param()) }
                        .findAny()
                    val argb: RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument(param, type)
                        .suggests(SuggestionProvider<S?> { context: CommandContext<S?>, builder: SuggestionsBuilder ->
                            if (parameter.isVarArgs) { //Do it before the builder is used
                                val nextTokenStart = context.getInput().lastIndexOf(' ') + 1
                                builder = builder.createOffset(nextTokenStart)
                            }
                            if (customTC.isPresent) for (ctc in customTC.get()) builder.suggest(ctc)
                            var ignoreCustomParamType = false
                            if (customTCmethod.isPresent) {
                                val tr = customTCmethod.get()
                                if (tr.value1.ignoreTypeCompletion()) ignoreCustomParamType = true
                                val method = tr.value2
                                val params = method.parameters
                                val args = arrayOfNulls<Any>(params.size)
                                var j = 0
                                var k = 0
                                while (j < args.size && k < subcmd.parameters.length) {
                                    val paramObj = params[j]
                                    if (CommandSender::class.java.isAssignableFrom(paramObj.type)) {
                                        args[j] = commodore!!.getBukkitSender(context.getSource())
                                        j++
                                        continue
                                    }
                                    val paramValueString = context.getArgument(subcmd.parameters.get(k), String::class.java)
                                    if (paramObj.type == String::class.java) {
                                        args[j] = paramValueString
                                        j++
                                        continue
                                    }
                                    //Break if converter is not found or for example, the player provided an invalid plugin name
                                    val converter = getParamConverter(params[j].type, command2MC) ?: break
                                    val paramValue = converter.converter.apply(paramValueString) ?: break
                                    args[j] = paramValue
                                    k++ //Only increment if not CommandSender
                                    j++
                                }
                                if (args.size == 0 || args[args.size - 1] != null) { //Arguments filled entirely
                                    try {
                                        val suggestions = method.invoke(command2MC, *args)
                                        if (suggestions is Iterable<*>) {
                                            for (suggestion in suggestions) if (suggestion is String) builder.suggest(suggestion as String?) else throw ClassCastException("Bad return type! It should return an Iterable<String> or a String[].")
                                        } else if (suggestions is Array<String>) for (suggestion in suggestions as Array<String?>) builder.suggest(suggestion) else throw ClassCastException("Bad return type! It should return a String[] or an Iterable<String>.")
                                    } catch (e: Exception) {
                                        val msg = "Failed to run tabcomplete method " + method.name + " for command " + command2MC.javaClass.simpleName
                                        if (command2MC.component == null) TBMCCoreAPI.SendException(msg, e, command2MC.plugin) else TBMCCoreAPI.SendException(msg, e, command2MC.component)
                                    }
                                }
                            }
                            if (!ignoreCustomParamType && customParamType) {
                                val converter = getParamConverter(ptype, command2MC)
                                if (converter != null) {
                                    val suggestions = converter.allSupplier.get()
                                    for (suggestion in suggestions) builder.suggest(suggestion)
                                }
                            }
                            if (ptype === Boolean::class.javaPrimitiveType || ptype === Boolean::class.java) builder.suggest("true").suggest("false")
                            val loweredInput = builder.remaining.lowercase(Locale.getDefault())
                            builder.suggest(param).buildFuture().whenComplete(BiConsumer<Suggestions, Throwable> { s: Suggestions, e: Throwable? ->  //The list is automatically ordered
                                s.list.add(s.list.removeAt(0))
                            }) //So we need to put the <param> at the end after that
                                .whenComplete(BiConsumer<Suggestions, Throwable> { ss: Suggestions, e: Throwable? ->
                                    ss.list.removeIf { s: Suggestion ->
                                        val text = s.text
                                        !text.startsWith("<") && !text.startsWith("[") && !text.lowercase(Locale.getDefault()).startsWith(loweredInput)
                                    }
                                })
                        })
                    val arg: ArgumentCommandNode<S, T> = argb.build()
                    scmd.addChild(arg)
                    scmd = arg
                }
            }
            if (shouldRegister.get()) {
                commodore.register(maincmd)
                //MinecraftArgumentTypes.getByKey(NamespacedKey.minecraft(""))
                val pluginName = command2MC.plugin.name.lowercase(Locale.getDefault())
                val prefixedcmd = LiteralArgumentBuilder.literal<Any>(pluginName + ":" + path.get(0))
                    .redirect(maincmd).build()
                commodore!!.register(prefixedcmd)
                for (alias in bukkitCommand.aliases) {
                    commodore!!.register(LiteralArgumentBuilder.literal<Any>(alias).redirect(maincmd).build())
                    commodore!!.register(LiteralArgumentBuilder.literal<Any>("$pluginName:$alias").redirect(maincmd).build())
                }
            }
        }
    }

    companion object {
        private fun getParamConverter(cl: Class<*>, command2MC: ICommand2MC): ParamConverter<*>? {
            val converter = ButtonPlugin.getCommand2MC().paramConverters[cl]
            if (converter == null) {
                val msg = "Could not find a suitable converter for type " + cl.simpleName
                val exception: Exception = NullPointerException("converter is null")
                if (command2MC.component == null) TBMCCoreAPI.SendException(msg, exception, command2MC.plugin) else TBMCCoreAPI.SendException(msg, exception, command2MC.component)
                return null
            }
            return converter
        }
    }
}