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
    private val cm: ChatMessage,
    rtr: RecipientTestResult
) : TBMCChatEventBase(channel, cm.message, rtr.score, rtr.groupID!!) {

    private val isIgnoreSenderPermissions: Boolean get() = cm.permCheck !== cm.sender

    /**
     * This will allow the sender of the message if [.isIgnoreSenderPermissions] is true.
     */
    override fun shouldSendTo(sender: CommandSender): Boolean {
        return if (isIgnoreSenderPermissions && sender == cm.sender) true else super.shouldSendTo(sender) //Allow sending the message no matter what
    }

    /**
     * This will allow the sender of the message if [.isIgnoreSenderPermissions] is true.
     */
    override fun getMCScore(sender: CommandSender): Int {
        return if (isIgnoreSenderPermissions && sender == cm.sender) score else super.getMCScore(sender) //Send in the correct group no matter what
    }

    /**
     * This will allow the sender of the message if [.isIgnoreSenderPermissions] is true.
     */
    override fun getGroupID(sender: CommandSender): String? {
        return if (isIgnoreSenderPermissions && sender == cm.sender) groupID else super.getGroupID(sender) //Send in the correct group no matter what
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    val sender: CommandSender get() = cm.sender
    val user: ChromaGamerBase get() = cm.user
    val origin: String get() = cm.origin

    companion object {
        val handlerList = HandlerList()
    }
}