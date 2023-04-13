package buttondevteam.core.component.channel

import buttondevteam.lib.TBMCSystemChatEvent
import buttondevteam.lib.chat.Color
import buttondevteam.lib.chat.TBMCChatAPI
import org.bukkit.command.CommandSender

class ChatRoom(displayname: String, color: Color, command: String) : Channel(
    displayname, color, command, null // TODO: Custom filter for rooms using abstract method
) {
    private val usersInRoom: MutableList<CommandSender> = ArrayList()
    private fun isInRoom(sender: CommandSender): Boolean {
        return usersInRoom.contains(sender)
    }

    fun joinRoom(sender: CommandSender) {
        usersInRoom.add(sender)
        TBMCChatAPI.SendSystemMessage(
            this,
            RecipientTestResult.ALL,
            sender.name + " joined the room",
            TBMCSystemChatEvent.BroadcastTarget.ALL
        ) //Always show message in the same kind of channel
    }

    fun leaveRoom(sender: CommandSender) {
        usersInRoom.remove(sender)
        TBMCChatAPI.SendSystemMessage(
            this,
            RecipientTestResult.ALL,
            sender.name + " left the room",
            ChannelComponent.roomJoinLeave
        )
    }
}