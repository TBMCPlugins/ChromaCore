package buttondevteam.lib

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.player.ChromaGamerBase
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
    @JvmField
    var isCancelled: Boolean = false

    /**
     * Note: Errors are sent to the sender automatically
     */
    open fun shouldSendTo(sender: ChromaGamerBase): Boolean {
        return channel.shouldSendTo(sender, score)
    }

    /**
     * Note: Errors are sent to the sender automatically
     */
    open fun getMCScore(sender: ChromaGamerBase): Int {
        return channel.getMCScore(sender)
    }

    /**
     * Note: Errors are sent to the sender automatically
     *
     * Null means don't send
     */
    open fun getGroupID(sender: ChromaGamerBase): String? {
        return channel.getGroupID(sender)
    }

    override fun isCancelled(): Boolean = isCancelled
    override fun setCancelled(cancel: Boolean) = run { isCancelled = cancel }
}