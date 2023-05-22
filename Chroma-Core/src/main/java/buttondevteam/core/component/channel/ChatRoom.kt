package buttondevteam.core.component.channel

import buttondevteam.lib.TBMCSystemChatEvent
import buttondevteam.lib.chat.Color
import buttondevteam.lib.chat.TBMCChatAPI
import buttondevteam.lib.player.ChromaGamerBase

class ChatRoom(displayname: String, color: Color, command: String) : Channel(
    displayname, color, command, null // TODO: Custom filter for rooms using abstract method
) {
    private val usersInRoom: MutableList<ChromaGamerBase> = ArrayList()
    private fun isInRoom(sender: ChromaGamerBase): Boolean {
        return usersInRoom.contains(sender)
    }

    fun joinRoom(sender: ChromaGamerBase) {
        usersInRoom.add(sender)
        TBMCChatAPI.SendSystemMessage(
            this,
            RecipientTestResult.ALL,
            sender.name + " joined the room",
            TBMCSystemChatEvent.BroadcastTarget.ALL
        ) //Always show message in the same kind of channel
    }

    fun leaveRoom(sender: ChromaGamerBase) {
        usersInRoom.remove(sender)
        TBMCChatAPI.SendSystemMessage(
            this,
            RecipientTestResult.ALL,
            sender.name + " left the room",
            ChannelComponent.roomJoinLeave
        )
    }
}