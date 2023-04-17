package buttondevteam.lib.chat

import buttondevteam.core.component.channel.Channel
import org.bukkit.command.CommandSender

class Command2MCSender(val sender: CommandSender, val channel: Channel, val permCheck: CommandSender) : Command2Sender {

    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun sendMessage(message: Array<String>) {
        sender.sendMessage(*message)
    }

    override fun getName(): String {
        return sender.name
    }
}