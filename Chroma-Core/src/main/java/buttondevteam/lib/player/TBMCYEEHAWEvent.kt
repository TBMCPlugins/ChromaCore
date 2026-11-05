package buttondevteam.lib.player

import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TBMCYEEHAWEvent(val sender: CommandSender?) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}
