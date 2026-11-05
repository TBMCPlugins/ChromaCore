package buttondevteam.lib.player

import java.lang.annotation.Inherited

/**
 * Specifies a [ChromaGamerBase] direct subclass which can be instantiated. For Minecraft data, use [PlayerClass]
 * 
 * @author NorbiPeti
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class UserClass(
    /**
     * Indicates which folder should the player files be saved in. Must be lowercase.
     */
    val foldername: String
)
