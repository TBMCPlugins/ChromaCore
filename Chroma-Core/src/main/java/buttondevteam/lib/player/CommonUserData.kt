package buttondevteam.lib.player

import org.bukkit.configuration.file.YamlConfiguration

/**
 * Per user, regardless of actual type
 *
 * @param <T> The user class, may be abstract
</T> */
class CommonUserData<T : ChromaGamerBase>(@JvmField val playerData: YamlConfiguration) {
    @JvmField
    val userCache: HashMap<Class<out T>, out T> = HashMap()
}