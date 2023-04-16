package buttondevteam.lib.player

import buttondevteam.lib.utils.TypedMap
import org.bukkit.configuration.file.YamlConfiguration

/**
 * Per user, regardless of actual type
 *
 * @param T The user class, may be abstract
</T> */
class CommonUserData<T : ChromaGamerBase>(@JvmField val playerData: YamlConfiguration) {
    /**
     * Caches users of the given user type. This is used to avoid loading the same user multiple times.
     * Also contains instances of subclasses of the given user type.
     */
    @JvmField
    val userCache: TypedMap<T> = TypedMap()
}