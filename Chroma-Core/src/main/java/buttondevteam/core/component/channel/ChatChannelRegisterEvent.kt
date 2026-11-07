package buttondevteam.core.component.channel

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ChatChannelRegisterEvent(val channel: Channel) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}
