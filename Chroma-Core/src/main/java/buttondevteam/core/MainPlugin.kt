package buttondevteam.core

import buttondevteam.core.component.channel.Channel
import buttondevteam.core.component.channel.ChannelComponent
import buttondevteam.core.component.channel.ChatRoom
import buttondevteam.core.component.members.MemberComponent
import buttondevteam.core.component.randomtp.RandomTPComponent
import buttondevteam.core.component.restart.RestartComponent
import buttondevteam.core.component.spawn.SpawnComponent
import buttondevteam.core.component.towny.TownyComponent
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.architecture.Component.Companion.registerComponent
import buttondevteam.lib.chat.Color
import buttondevteam.lib.chat.TBMCChatAPI
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayer
import buttondevteam.lib.player.TBMCPlayerBase
import com.earth2me.essentials.Essentials
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Supplier

class MainPlugin : ButtonPlugin() {
    private var economy: Economy? = null

    /**
     * Whether the Core's chat handler should be enabled.
     * Other chat plugins handling messages from other platforms should set this to false.
     */
    var isChatHandlerEnabled = true

    /**
     * The chat format to use for messages from other platforms if Chroma-Chat is not installed.
     */
    @JvmField
    var chatFormat = iConfig.getData("chatFormat", "[{origin}|{channel}] <{name}> {message}")

    /**
     * Print some debug information.
     */
    @JvmField
    val test = iConfig.getData("test", false)

    /**
     * If a Chroma command clashes with another plugin's command, this setting determines whether the Chroma command should be executed or the other plugin's.
     */
    val prioritizeCustomCommands = iConfig.getData("prioritizeCustomCommands", false)
    public override fun pluginEnable() {
        instance = this
        val pdf = description
        if (!setupPermissions()) throw NullPointerException("No permission plugin found!")
        if (!setupEconomy()) //Though Essentials always provides economy, but we don't require Essentials
            logger.warning("No economy plugin found! Components using economy will not be registered.")
        saveConfig()
        registerComponent(this, RestartComponent())
        registerComponent(this, ChannelComponent())
        registerComponent(this, RandomTPComponent())
        registerComponent(this, MemberComponent())
        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core"))
            registerComponent(this, SpawnComponent())
        if (Bukkit.getPluginManager().isPluginEnabled("Towny")) //It fails to load the component class otherwise
            registerComponent(this, TownyComponent())
        /*if (Bukkit.getPluginManager().isPluginEnabled("Votifier") && economy != null)
			Component.registerComponent(this, new VotifierComponent(economy));*/
        ComponentManager.enableComponents()
        registerCommand(ComponentCommand())
        registerCommand(ChromaCommand())
        TBMCCoreAPI.RegisterEventsForExceptions(PlayerListener(this), this)
        TBMCCoreAPI.RegisterEventsForExceptions(command2MC, this)
        //Console & cmdblocks
        ChromaGamerBase.addConverter { commandSender: CommandSender ->
            Optional.ofNullable(
                if (commandSender is ConsoleCommandSender || commandSender is BlockCommandSender)
                    TBMCPlayerBase.getPlayer(UUID(0, 0), TBMCPlayer::class.java)
                else null
            )
        }
        //Players, has higher priority
        ChromaGamerBase.addConverter { sender: CommandSender ->
            Optional.ofNullable(
                if (sender is Player) TBMCPlayerBase.getPlayer(sender.uniqueId, TBMCPlayer::class.java) else null
            )
        }
        TBMCCoreAPI.RegisterUserClass(TBMCPlayerBase::class.java) { TBMCPlayer() }
        TBMCChatAPI.registerChatChannel(Channel("§fg§f", Color.White, "g", null)
            .also { Channel.globalChat = it }) //The /ooc ID has moved to the config
        TBMCChatAPI.registerChatChannel(Channel("§cADMIN§f", Color.Red, "a", Channel.inGroupFilter(null))
            .also { Channel.adminChat = it })
        TBMCChatAPI.registerChatChannel(Channel("§9MOD§f", Color.Blue, "mod", Channel.inGroupFilter("mod"))
            .also { Channel.modChat = it })
        TBMCChatAPI.registerChatChannel(
            Channel(
                "§6DEV§f",
                Color.Gold,
                "dev",
                Channel.inGroupFilter("developer")
            )
        ) // TODO: Make groups configurable
        TBMCChatAPI.registerChatChannel(ChatRoom("§cRED§f", Color.DarkRed, "red"))
        TBMCChatAPI.registerChatChannel(ChatRoom("§6ORANGE§f", Color.Gold, "orange"))
        TBMCChatAPI.registerChatChannel(ChatRoom("§eYELLOW§f", Color.Yellow, "yellow"))
        TBMCChatAPI.registerChatChannel(ChatRoom("§aGREEN§f", Color.Green, "green"))
        TBMCChatAPI.registerChatChannel(ChatRoom("§bBLUE§f", Color.Blue, "blue"))
        TBMCChatAPI.registerChatChannel(ChatRoom("§5PURPLE§f", Color.DarkPurple, "purple"))
        val playerSupplier = Supplier { Bukkit.getOnlinePlayers().map { obj -> obj.name }.asIterable() }
        command2MC.addParamConverter(
            OfflinePlayer::class.java,
            { name -> @Suppress("DEPRECATION") Bukkit.getOfflinePlayer(name) },
            "Player not found!",
            playerSupplier
        )
        command2MC.addParamConverter(
            Player::class.java,
            { name -> Bukkit.getPlayer(name) },
            "Online player not found!",
            playerSupplier
        )
        if (server.pluginManager.isPluginEnabled("Essentials")) ess = getPlugin(Essentials::class.java)
        logger.info(pdf.name + " has been Enabled (V." + pdf.version + ") Test: " + test.get() + ".")
    }

    public override fun pluginDisable() {
        logger.info("Saving player data...")
        ChromaGamerBase.saveUsers()
        logger.info("Player data saved.")
    }

    private fun setupPermissions(): Boolean {
        permission = setupProvider(Permission::class.java)
        return permission != null
    }

    private fun setupEconomy(): Boolean {
        economy = setupProvider(Economy::class.java)
        return economy != null
    }

    private fun <T> setupProvider(cl: Class<T>): T? {
        val provider = server.servicesManager
            .getRegistration(cl)
        return provider?.provider
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (command.name == "dontrunthiscmd") return true //Used in chat preprocess for console
        sender.sendMessage("§cThis command isn't available.") //In theory, unregistered commands use this method
        return true
    }

    companion object {
        lateinit var instance: MainPlugin

        @JvmField
        var permission: Permission? = null

        @JvmField
        var ess: Essentials? = null
    }
}