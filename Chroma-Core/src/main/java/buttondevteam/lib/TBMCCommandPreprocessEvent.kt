package buttondevteam.lib

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.player.ChromaGamerBase
import org.bukkit.command.CommandSender
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Can be used to change or handle commands before they're sent.
 * **Called on using player, console and Discord commands.**
 *
 * @author NorbiPeti
 */
class TBMCCommandPreprocessEvent(
    val sender: ChromaGamerBase,
    val channel: Channel,
    val message: String,
    val permCheck: CommandSender
) : Event(), Cancellable {
    private var cancelled = false
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled() = cancelled

    override fun setCancelled(cancelled: Boolean) = run { this.cancelled = cancelled }

    companion object {
        val handlerList = HandlerList()
    }
}