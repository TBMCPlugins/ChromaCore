package buttondevteam.lib

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TBMCDebugMessageEvent(
    /**
     * Gets the message (where did this exception occur, etc.)
     * 
     * @return The message
     */
    val debugMessage: String?
) : Event(!Bukkit.isPrimaryThread()) {
    /**
     * Gets if this event was handled
     * 
     * @return True if it was handled
     */
    var isSent: Boolean = false
        private set

    /**
     * Flags the event as handled
     */
    fun setSent() {
        this.isSent = true
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}
