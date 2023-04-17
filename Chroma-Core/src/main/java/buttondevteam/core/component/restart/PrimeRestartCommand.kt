package buttondevteam.core.component.restart

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.chat.Command2.*
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.chat.TBMCChatAPI.SendSystemMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

@CommandClass(
    path = "primerestart", modOnly = true, helpText = ["§6---- Prime restart ----",  //
        "Restarts the server as soon as nobody is online.",  //
        "To be loud, type something after, like /primerestart lol (it doesn't matter what you write)",  //
        "To be silent, don't type anything" //
    ]
)
class PrimeRestartCommand : ICommand2MC() {
    @Subcommand
    fun def(sender: CommandSender, @TextArg @OptionalArg somethingrandom: String?) {
        val isLoud = somethingrandom != null
        val component = component as RestartComponent
        component.isLoud = isLoud
        if (Bukkit.getOnlinePlayers().isNotEmpty()) {
            sender.sendMessage("§bPlayers online, restart delayed.")
            if (isLoud) SendSystemMessage(
                Channel.globalChat,
                Channel.RecipientTestResult.ALL,
                "${ChatColor.DARK_RED}The server will restart as soon as nobody is online.",
                component.restartBroadcast
            )
            component.isPlsrestart = true
        } else {
            sender.sendMessage("§bNobody is online. Restarting now.")
            if (isLoud) SendSystemMessage(
                Channel.globalChat,
                Channel.RecipientTestResult.ALL,
                "${ChatColor.RED}Nobody is online. Restarting server.",
                component.restartBroadcast
            )
            Bukkit.spigot().restart()
        }
    }

    companion object {
    }
}