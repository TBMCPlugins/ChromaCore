package buttondevteam.lib.utils

import kotlin.reflect.KClass

/**
 * A map that can be used to store data of different types. The data is stored in a map, but the type is checked when getting
 * the data.
 *
 * @param T The upper bound of the types that can be stored in this map
 */
class TypedMap<T : Any> {
    private val map = HashMap<KClass<*>, T>()
    fun <S : T> put(value: S) {
        map[value::class] = value
    }

    operator fun <S : T> get(type: Class<S>): S? {
        @Suppress("UNCHECKED_CAST")
        return map[type.kotlin] as S?
    }

    fun containsKey(type: Class<in T>): Boolean {
        return map.containsKey(type.kotlin)
    }

    fun remove(type: Class<in T>): T? {
        return map.remove(type.kotlin)
    }
}