package buttondevteam.lib

import buttondevteam.core.component.channel.Channel
import org.bukkit.command.CommandSender
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

abstract class TBMCChatEventBase(
    val channel: Channel,
    open val message: String,
    /**
     * The sender's score.
     */
    val score: Int,
    /**
     * The sender's group ID.
     */
    val groupID: String,
) : Event(true), Cancellable {
    var isCancelled: Boolean = false

    /**
     * Note: Errors are sent to the sender automatically
     */
    open fun shouldSendTo(sender: CommandSender): Boolean {
        return channel.shouldSendTo(sender, score)
    }

    /**
     * Note: Errors are sent to the sender automatically
     */
    open fun getMCScore(sender: CommandSender): Int {
        return channel.getMCScore(sender)
    }

    /**
     * Note: Errors are sent to the sender automatically<br></br>
     *
     * Null means don't send
     */
    open fun getGroupID(sender: CommandSender): String? {
        return channel.getGroupID(sender)
    }

    override fun isCancelled(): Boolean = isCancelled
    override fun setCancelled(cancel: Boolean) = run { isCancelled = cancel }
}