package buttondevteam.core

import buttondevteam.lib.TBMCChatEvent
import buttondevteam.lib.TBMCCommandPreprocessEvent
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.TBMCSystemChatEvent
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.chat.ChatMessage
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.chat.TBMCChatAPI
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayer
import buttondevteam.lib.player.TBMCPlayerBase
import buttondevteam.lib.player.TBMCPlayerBase.Companion.asTBMC
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerCommandEvent

class PlayerListener(val plugin: MainPlugin) : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val p = event.player
        val player = TBMCPlayerBase.getPlayer(p.uniqueId, TBMCPlayer::class.java)
        val pname = player.playerName.get()
        if (pname.isEmpty()) {
            player.playerName.set(p.name)
            plugin.logger.info("Player name saved: " + player.playerName.get())
        } else if (p.name != pname) {
            plugin.logger.info(pname + " renamed to " + p.name)
            player.playerName.set(p.name)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerLeave(event: PlayerQuitEvent) {
        TBMCPlayerBase.getPlayer(event.player.uniqueId, TBMCPlayer::class.java).uncache()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onSystemChat(event: TBMCSystemChatEvent) {
        if (event.isHandled) return
        if (event.exceptions.any { "Minecraft".equals(it, ignoreCase = true) }) return
        Bukkit.getOnlinePlayers().stream().filter { event.shouldSendTo(it.asTBMC()) }
            .forEach { it.sendMessage(event.channel.displayName.get().substring(0, 2) + event.message) }
        // TODO: Replace the substring call with a proper color code getter.
    }

    @EventHandler
    fun onPlayerChatPreprocess(event: PlayerCommandPreprocessEvent) {
        handlePreprocess(event.player, event.message, event)
    }

    @EventHandler
    fun onSystemChatPreprocess(event: ServerCommandEvent) {
        handlePreprocess(event.sender, "/" + event.command, event)
        if (event.isCancelled) event.command = "dontrunthiscmd" //Bugfix
    }

    private fun handlePreprocess(sender: CommandSender, message: String, event: Cancellable) {
        if (event.isCancelled) return
        val cg = ChromaGamerBase.getFromSender(sender)
        val ev = TBMCCommandPreprocessEvent(cg, cg.channel.get(), message, sender)
        Bukkit.getPluginManager().callEvent(ev)
        if (ev.isCancelled) event.isCancelled = true //Cancel the original event
    }

    @EventHandler
    fun onTBMCPreprocess(event: TBMCCommandPreprocessEvent) {
        if (event.isCancelled) return
        try {
            val mcuser = event.sender.getAs(TBMCPlayerBase::class.java)
            if (mcuser == null) { // TODO: The chat should continue to support unconnected accounts.
                event.sender.sendMessage("You need to have your Minecraft account connected to send commands.")
                event.isCancelled = true
                return
            }
            val sender = Command2MCSender(mcuser, event.channel, event.permCheck)
            event.isCancelled = ButtonPlugin.command2MC.handleCommand(sender, event.message)
        } catch (e: Exception) {
            TBMCCoreAPI.SendException(
                "Command processing failed for sender '${event.sender}' and message '${event.message}'",
                e,
                plugin
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGH) //The one in the chat plugin is set to highest
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (event.isCancelled) return  //The chat plugin should cancel it after this handler
        val cp = TBMCPlayerBase.getPlayer(event.player.uniqueId, TBMCPlayer::class.java)
        TBMCChatAPI.sendChatMessage(ChatMessage.builder(cp, event.message).build())
        //Not cancelling the original event here, it's cancelled in the chat plugin
        //This way other plugins can deal with the MC formatting if the chat plugin isn't present, but other platforms still get the message
    }

    @EventHandler(priority = EventPriority.HIGH) //The one in the chat plugin is set to highest
    fun onPlayerChat(event: TBMCChatEvent) {
        if (event.isCancelled) return
        if (!plugin.isChatHandlerEnabled) return
        if (event.origin == "Minecraft") return  //Let other plugins handle MC messages
        val channel = event.channel
        val msg = plugin.chatFormat.get()
            .replace("{channel}", channel.displayName.get())
            .replace("{origin}", event.origin.substring(0, 1))
            .replace("{name}", event.user.name)
            .replace("{message}", String.format("§%x%s", channel.color.get().ordinal, event.message))
        for (player in Bukkit.getOnlinePlayers()) if (event.shouldSendTo(player.asTBMC())) player.sendMessage(msg)
        Bukkit.getConsoleSender().sendMessage(msg)
    }
}