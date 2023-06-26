package buttondevteam.lib

import buttondevteam.core.component.restart.ScheduledRestartCommand
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ScheduledServerRestartEvent(val restartTicks: Int, val command: ScheduledRestartCommand) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}