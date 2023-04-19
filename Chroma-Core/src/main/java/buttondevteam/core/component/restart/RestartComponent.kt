package buttondevteam.core.component.restart

import buttondevteam.core.MainPlugin
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget
import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget.Companion.add
import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget.Companion.remove
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.ComponentMetadata
import buttondevteam.lib.chat.IFakePlayer
import buttondevteam.lib.chat.TBMCChatAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Provides commands such as /schrestart (restart after a countdown) and /primerestart (restart when nobody is online).
 * Also can automatically restart at a given time.
 */
@ComponentMetadata(enabledByDefault = false)
class RestartComponent : Component<MainPlugin>(), Listener {
    public override fun enable() {
        val scheduledRestartCommand = ScheduledRestartCommand()
        registerCommand(scheduledRestartCommand)
        registerCommand(PrimeRestartCommand())
        registerListener(this)
        restartBroadcast = add("restartCountdown")
        val restartAt = restartAt.get()
        if (restartAt < 0) return
        val restart = syncStart(restartAt)
        log("Scheduled restart " + restart / 3600.0 / 20.0 + " hours from now")
        Bukkit.getScheduler().runTaskLater(
            plugin,
            Runnable { scheduledRestartCommand.def(Bukkit.getConsoleSender(), 0) },
            restart.toLong()
        )
    }

    public override fun disable() {
        remove(restartBroadcast)
    }

    /**
     * Specifies the hour of day when the server should be restarted. Set to -1 to disable.
     */
    private val restartAt = config.getData("restartAt", 12)
    private var lasttime: Long = 0

    var isPlsrestart = false
    var isLoud = false

    lateinit var restartBroadcast: BroadcastTarget
    private fun syncStart(hour: Int): Int {
        val now = ZonedDateTime.now(ZoneId.ofOffset("", ZoneOffset.UTC))
        val secs = now.hour * 3600 + now.minute * 60 + now.second
        var diff = secs - hour * 3600
        if (diff < 0) {
            diff += 24 * 3600
        }
        val count = diff / (24 * 3600)
        val intervalPart = diff - count * 24 * 3600
        val remaining = 24 * 3600 - intervalPart
        return remaining * 20
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (isPlsrestart
            && !event.quitMessage.equals("Server closed", ignoreCase = true)
            && !event.quitMessage.equals("Server is restarting", ignoreCase = true)
        ) {
            if (Bukkit.getOnlinePlayers().size <= 1) {
                if (isLoud) TBMCChatAPI.SendSystemMessage(
                    Channel.globalChat,
                    Channel.RecipientTestResult.ALL,
                    "${ChatColor.RED}Nobody is online anymore. Restarting.",
                    restartBroadcast
                )
                Bukkit.spigot().restart()
            } else if (event.player !is IFakePlayer && System.nanoTime() - 10 * 60 * 1000000000L - lasttime > 0) { //10 minutes passed since last reminder
                lasttime = System.nanoTime()
                if (isLoud) TBMCChatAPI.SendSystemMessage(
                    Channel.globalChat,
                    Channel.RecipientTestResult.ALL,
                    ChatColor.DARK_RED.toString() + "The server will restart as soon as nobody is online.",
                    restartBroadcast
                )
            }
        }
    }
}