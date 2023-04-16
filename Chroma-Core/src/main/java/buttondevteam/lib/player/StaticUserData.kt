package buttondevteam.lib.player

import java.util.function.Supplier

/**
 * Per user class
 *
 * @param T The user class type, may be abstract
 */
class StaticUserData<T : ChromaGamerBase>(val folder: String) {
    /**
     * Constructors for the subclasses of the given user type.
     */
    val constructors = HashMap<Class<out T>, Supplier<T>>()

    /**
     * Key: User ID
     */
    val userDataMap = HashMap<String, CommonUserData<T>>()

}