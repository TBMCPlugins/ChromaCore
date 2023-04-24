package buttondevteam.lib

import buttondevteam.core.component.channel.Channel
import buttondevteam.core.component.channel.Channel.RecipientTestResult
import buttondevteam.lib.chat.ChatMessage
import buttondevteam.lib.player.ChromaGamerBase
import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList

/**
 * Make sure to only send the message to users where [.shouldSendTo] returns true.
 *
 * @author NorbiPeti
 */
class TBMCChatEvent(
    channel: Channel,
    public val chatMessage: ChatMessage,
    rtr: RecipientTestResult
) : TBMCChatEventBase(channel, chatMessage.message, rtr.score, rtr.groupID!!) {

    private val isIgnoreSenderPermissions: Boolean get() = chatMessage.permCheck !== chatMessage.sender

    /**
     * This will allow the sender of the message if [.isIgnoreSenderPermissions] is true.
     */
    override fun shouldSendTo(sender: CommandSender): Boolean {
        return if (isIgnoreSenderPermissions && sender == chatMessage.sender) true else super.shouldSendTo(sender) //Allow sending the message no matter what
    }

    /**
     * This will allow the sender of the message if [.isIgnoreSenderPermissions] is true.
     */
    override fun getMCScore(sender: CommandSender): Int {
        return if (isIgnoreSenderPermissions && sender == chatMessage.sender) score else super.getMCScore(sender) //Send in the correct group no matter what
    }

    /**
     * This will allow the sender of the message if [.isIgnoreSenderPermissions] is true.
     */
    override fun getGroupID(sender: CommandSender): String? {
        return if (isIgnoreSenderPermissions && sender == chatMessage.sender) groupID else super.getGroupID(sender) //Send in the correct group no matter what
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    val sender: CommandSender get() = chatMessage.sender
    val user: ChromaGamerBase get() = chatMessage.user
    val origin: String get() = chatMessage.origin
    val isFromCommand get() = chatMessage.isFromCommand

    companion object {
        val handlerList = HandlerList()
    }
}