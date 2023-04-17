package buttondevteam.lib

import buttondevteam.core.component.channel.Channel
import org.bukkit.event.HandlerList
import java.util.*
import java.util.stream.Stream

/**
 * Make sure to only send the message to users who [.shouldSendTo] returns true.
 *
 * @author NorbiPeti
 */ // TODO: Rich message
class TBMCSystemChatEvent(
    channel: Channel,
    message: String,
    score: Int,
    groupid: String,
    val exceptions: Array<out String>,
    val target: BroadcastTarget
) : TBMCChatEventBase(channel, message, score, groupid) {
    var isHandled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    class BroadcastTarget private constructor(val name: String) {

        companion object {
            private val targets = HashSet<BroadcastTarget?>()
            val ALL = BroadcastTarget("ALL")

            @JvmStatic
            fun add(name: String): BroadcastTarget {
                val bt = BroadcastTarget(Objects.requireNonNull(name))
                targets.add(bt)
                return bt
            }

            @JvmStatic
            fun remove(target: BroadcastTarget?) {
                targets.remove(target)
            }

            operator fun get(name: String?): BroadcastTarget? {
                return targets.stream().filter { bt: BroadcastTarget? -> bt!!.name.equals(name, ignoreCase = true) }
                    .findAny().orElse(null)
            }

            fun stream(): Stream<BroadcastTarget?> {
                return targets.stream()
            }
        }
    }

    companion object {
        val handlerList = HandlerList()
    }
}