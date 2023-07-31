package buttondevteam.core

import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.architecture.ButtonPlugin.Companion.command2MC
import buttondevteam.lib.chat.Command2.OptionalArg
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

@CommandClass
class ChromaCommand : ICommand2MC() {
    init {
        manager.addParamConverter(
            ButtonPlugin::class.java, { name ->
                Bukkit.getPluginManager().getPlugin(name)
                    ?.let { if (it is ButtonPlugin) it else null }
            },
            "No Chroma plugin found by that name."
        ) {
            Iterable {
                Arrays.stream(Bukkit.getPluginManager().plugins)
                    .filter { it is ButtonPlugin }.map { it.name }.iterator()
            }
        }
    }

    @Subcommand
    fun reload(sender: CommandSender, @OptionalArg plugin: ButtonPlugin?) {
        val pl = plugin ?: this.plugin
        if (pl.tryReloadConfig())
            sender.sendMessage("${ChatColor.AQUA}${pl.name} config reloaded.")
        else
            sender.sendMessage("${ChatColor.RED}Failed to reload config. Check console.")
    }

    @Subcommand
    override fun def(sender: Command2MCSender): Boolean {
        sender.sendMessage("${ChatColor.GOLD}---- Commands ----")
        sender.sendMessage(command2MC.getCommandList(sender))
        return true
    }
}
