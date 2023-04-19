package buttondevteam.core.component.restart

import buttondevteam.core.MainPlugin
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.ScheduledServerRestartEvent
import buttondevteam.lib.chat.Command2.OptionalArg
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.chat.TBMCChatAPI.SendSystemMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitTask
import java.util.function.Consumer
import kotlin.properties.Delegates

@CommandClass(
    modOnly = true, path = "schrestart", helpText = ["Scheduled restart",  //
        "This command restarts the server 1 minute after it's executed, warning players every 10 seconds.",  //
        "You can optionally set the amount of seconds to wait before the restart." //
    ]
)
class ScheduledRestartCommand : ICommand2MC() {
    private var restartCounter = 0
    private lateinit var restartTask: BukkitTask

    @Volatile
    private lateinit var restartBar: BossBar

    private var restartInitialTicks by Delegates.notNull<Int>()

    @Subcommand
    fun def(sender: CommandSender, @OptionalArg seconds: Int): Boolean {
        return restart(sender, if (seconds == 0) 60 else seconds)
    }

    private fun restart(sender: CommandSender, seconds: Int): Boolean {
        if (seconds < 10) {
            sender.sendMessage("${ChatColor.RED}Error: Seconds must be at least 10.")
            return false
        }
        restartCounter = seconds * 20
        restartInitialTicks = restartCounter
        restartBar =
            Bukkit.createBossBar("Server restart in " + seconds + "s", BarColor.RED, BarStyle.SOLID, BarFlag.DARKEN_SKY)
        restartBar.progress = 1.0
        Bukkit.getOnlinePlayers().forEach { p -> restartBar.addPlayer(p) }
        sender.sendMessage("Scheduled restart in $seconds")
        val e = ScheduledServerRestartEvent(restartInitialTicks, this)
        Bukkit.getPluginManager().callEvent(e)
        restartTask = Bukkit.getScheduler().runTaskTimer(MainPlugin.instance, ::updateRestartTimer, 1, 1)
        return true
    }

    private fun updateRestartTimer() {
        if (restartCounter < 0) {
            restartTask.cancel()
            restartBar.players.forEach(Consumer { p -> restartBar.removePlayer(p) })
            Bukkit.spigot().restart()
        }
        if (restartCounter % 200 == 0 && Bukkit.getOnlinePlayers().isNotEmpty()) SendSystemMessage(
            Channel.globalChat,
            Channel.RecipientTestResult.ALL,
            "${ChatColor.RED}-- The server is restarting in " + restartCounter / 20 + " seconds!",
            (component as RestartComponent).restartBroadcast
        )
        restartBar.progress = restartCounter / restartInitialTicks.toDouble()
        restartBar.setTitle(String.format("Server restart in %.2f", restartCounter / 20f))
        restartCounter--
    }
}