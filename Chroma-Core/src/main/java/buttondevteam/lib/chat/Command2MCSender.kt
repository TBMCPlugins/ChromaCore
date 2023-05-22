package buttondevteam.lib.chat

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.player.TBMCPlayerBase
import org.bukkit.command.CommandSender

class Command2MCSender(val sender: TBMCPlayerBase, val channel: Channel, val permCheck: CommandSender) : Command2Sender {
    // TODO: Remove this class and only use the user classes.
    // TODO: The command context should be stored separately.
    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun sendMessage(message: Array<String>) {
        sender.sendMessage(message)
    }

    override fun getName(): String {
        return sender.name
    }
}