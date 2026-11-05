package buttondevteam.lib.player

import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Specifies a [ChromaGamerBase] direct subclass which's abstract. For Minecraft data, use [PlayerClass]
 * 
 * @author NorbiPeti
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class AbstractUserClass(
    /**
     * Indicates which folder should the player files be saved in.
     */
    val foldername: String,
    /**
     * Indicates the class to create when connecting accounts.
     */
    val prototype: KClass<out ChromaGamerBase>
)
