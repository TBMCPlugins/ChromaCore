package buttondevteam.lib

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.chat.Command2Sender
import net.kyori.adventure.text.Component
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Can be used to change messages before it's sent.
 * **Only called before sending messages with SendChatMessage.**
 *
 * @author NorbiPeti
 */
class TBMCChatPreprocessEvent(val sender: Command2Sender, val channel: Channel, var message: Component) : Event(true),
    Cancellable {
    private var cancelled = false
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled() = cancelled

    override fun setCancelled(cancelled: Boolean) = run { this.cancelled = cancelled }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}