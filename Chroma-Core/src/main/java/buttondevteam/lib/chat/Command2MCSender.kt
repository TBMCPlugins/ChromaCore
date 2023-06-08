package buttondevteam.lib.chat

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.player.ChromaGamerBase

class Command2MCSender(val sender: ChromaGamerBase, val channel: Channel, val permCheck: ChromaGamerBase) : Command2Sender {
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