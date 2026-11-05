package buttondevteam.lib.player

import buttondevteam.lib.ChromaUtils.isTest
import buttondevteam.lib.player.ChromaGamerBase.InfoTarget
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * This event gets called when player information is requested. It can be used to give more per-plugin information about a player.
 *
 * @author Norbi
 */
class TBMCPlayerGetInfoEvent internal constructor(
    /**
     * Get the [TBMCPlayer] object
     * 
     * @return A player object
     */
    val player: ChromaGamerBase?,
    /**
     * Get the [InfoTarget] we want the information for. Use this to format the returned string.
     * 
     * @return The target of the information.
     */
    val target: InfoTarget?
) : Event(!isTest) {
    private val infolines: MutableList<String?> = ArrayList()

    /**
     * Add a line to the information text. The line should be in the format of *key: value* .
     * 
     * @param infoline
     * The line to add
     */
    fun addInfo(infoline: String?) {
        infolines.add(infoline)
    }

    val result: String
        get() = infolines.joinToString("\n")

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}
