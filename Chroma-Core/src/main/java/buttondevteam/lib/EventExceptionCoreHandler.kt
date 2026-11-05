package buttondevteam.lib

import buttondevteam.lib.TBMCCoreAPI.SendException
import org.bukkit.Bukkit
import org.bukkit.event.Event

internal class EventExceptionCoreHandler : EventExceptionHandler() {
    override fun handle(ex: Throwable, event: Event): Boolean {
        SendException("An error occured while executing " + event.getEventName() + "!", ex, false) { msg -> Bukkit.getLogger().warning(msg) }
        return true
    }
}
