package buttondevteam.core.component.members

import buttondevteam.core.MainPlugin
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.ComponentMetadata
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Allows giving a 'member' group over some time elapsed OR played.
 */
@ComponentMetadata(enabledByDefault = false)
class MemberComponent : Component<MainPlugin>(), Listener {
    /**
     * The permission group to give to the player
     */
    val memberGroup get() = config.getData("memberGroup", "member")

    /**
     * The amount of hours needed to play before promotion
     */
    private val playedHours get() = config.getData("playedHours", 12)

    /**
     * The amount of days passed since first login
     */
    private val registeredForDays get() = config.getData("registeredForDays", 7)
    private var playtime: Pair<Statistic, Int>? = null
    override fun enable() {
        registerListener(this)
        registerCommand(MemberCommand())
        playtime = try {
            Pair(Statistic.valueOf("PLAY_ONE_MINUTE"), 60) //1.14
        } catch (e: IllegalArgumentException) {
            Pair(Statistic.valueOf("PLAY_ONE_TICK"), 20 * 3600) //1.12
        }
    }

    override fun disable() {}

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (checkNotMember(event.player) && (checkRegTime(event.player) || checkPlayTime(event.player))) {
            addPlayerAsMember(event.player)
        }
    }

    fun addPlayerAsMember(player: Player): Boolean? {
        return try {
            if (MainPlugin.permission.playerAddGroup(null, player, memberGroup.get())) {
                player.sendMessage("\${ChatColor.AQUA}You are a member now!")
                log("Added " + player.name + " as a member.")
                true
            } else {
                logWarn("Failed to assign the member role! Please make sure the member group exists or disable the component if it's unused.")
                false
            }
        } catch (e: UnsupportedOperationException) {
            logWarn("Failed to assign the member role! Groups are not supported by the permissions implementation.")
            null
        }
    }

    fun checkNotMember(player: Player?): Boolean {
        return !MainPlugin.permission.playerInGroup(player, memberGroup.get())
    }

    fun checkRegTime(player: Player): Boolean {
        return getRegTime(player) == -1L
    }

    fun checkPlayTime(player: Player): Boolean {
        return getPlayTime(player) > playtime!!.second * playedHours.get()
    }

    /**
     * Returns milliseconds
     */
    fun getRegTime(player: Player): Long {
        val date = Date(player.firstPlayed).toInstant().plus(registeredForDays.get().toLong(), ChronoUnit.DAYS)
        return if (date.isAfter(Instant.now())) date.toEpochMilli() - Instant.now().toEpochMilli() else -1
    }

    fun getPlayTimeTotal(player: Player): Int {
        return player.getStatistic(playtime!!.first)
    }

    /**
     * Returns hours
     */
    fun getPlayTime(player: Player): Double {
        val pt = playedHours.get() - getPlayTimeTotal(player).toDouble() / playtime!!.second
        return if (pt < 0) (-1).toDouble() else pt
    }
}
