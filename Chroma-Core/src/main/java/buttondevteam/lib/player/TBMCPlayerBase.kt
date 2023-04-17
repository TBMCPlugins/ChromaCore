package buttondevteam.lib.player

import buttondevteam.lib.architecture.IHaveConfig
import org.bukkit.Bukkit
import java.util.*

@AbstractUserClass(foldername = "minecraft", prototype = TBMCPlayer::class)
@TBMCPlayerEnforcer
abstract class TBMCPlayerBase : ChromaGamerBase() {
    val uniqueId: UUID by lazy { UUID.fromString(fileName) }

    @JvmField
    val playerName = super.config.getData("PlayerName", "")
    public override fun init() {
        super.init()
        val pluginName = if (javaClass.isAnnotationPresent(PlayerClass::class.java))
            javaClass.getAnnotation(PlayerClass::class.java).pluginname
        else
            throw RuntimeException("Class not defined as player class! Use @PlayerClass")
        val playerData = commonUserData.playerData
        val section = playerData.getConfigurationSection(pluginName) ?: playerData.createSection(pluginName)
        config = IHaveConfig({ save() }, section)
    }

    /**
     * If the player is online, we don't uncache, it will be uncached when they log out
     */
    override fun scheduleUncache() {
        val p = Bukkit.getPlayer(uniqueId)
        if (p == null || !p.isOnline) super.scheduleUncache()
    }

    override fun save() {
        val keys = commonUserData.playerData.getKeys(false)
        if (keys.size > 1) // PlayerName is always saved, but we don't need a file for just that
            super.save()
    }

    companion object {
        /**
         * Get player as a plugin player.
         *
         * @param uuid The UUID of the player to get
         * @param cl   The type of the player
         * @return The requested player object
         */
        fun <T : TBMCPlayerBase> getPlayer(uuid: UUID, cl: Class<T>): T {
            val player = getUser(uuid.toString(), cl)
            check(player.uniqueId == uuid) { "Player UUID differs after converting from and to string..." }
            return player
        }

        /**
         * This method returns a TBMC player from their name. See [Bukkit.getOfflinePlayer].
         *
         * @param name The player's name
         * @return The [TBMCPlayer] object for the player
         */
        @Suppress("deprecation")
        fun <T : TBMCPlayerBase> getFromName(name: String, cl: Class<T>): T {
            val p = Bukkit.getOfflinePlayer(name)
            return getPlayer(p.uniqueId, cl)
        }
    }
}