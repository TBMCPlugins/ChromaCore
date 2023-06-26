package buttondevteam.lib

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * This event gets called (ideally) each time an exception occurs in a TBMC plugin. To call it, use [TBMCCoreAPI.SendException].
 *
 *
 * @author Norbi
 */
class TBMCExceptionEvent(val sourceMessage: String, val exception: Throwable) : Event(!Bukkit.isPrimaryThread()) {
    var isHandled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}