package buttondevteam.lib

import buttondevteam.core.MainPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import java.util.function.Supplier

object ChromaUtils {
    @JvmStatic
    @Deprecated("Use ChromaGamingBase.name", ReplaceWith("ChromaGamerBase.getFromSender(sender).name"))
    fun getDisplayName(sender: CommandSender): String {
        return when (sender) {
            is IHaveFancyName -> sender.fancyName
            is Player -> sender.displayName
            else -> sender.name
        }
    }

    @JvmStatic
    fun getFullDisplayName(sender: CommandSender): String {
        return when (sender) {
            is IHaveFancyName -> sender.fancyFullName
            else -> getDisplayName(sender)
        }
    }

    @JvmStatic
    fun convertNumber(number: Number, targetcl: Class<out Number>): Number {
        return when {
            targetcl == Long::class.javaPrimitiveType || Long::class.javaObjectType.isAssignableFrom(targetcl) -> number.toLong()
            targetcl == Int::class.javaPrimitiveType || Int::class.javaObjectType.isAssignableFrom(targetcl) -> number.toInt() //Needed because the parser can get longs
            targetcl == Short::class.javaPrimitiveType || Short::class.javaObjectType.isAssignableFrom(targetcl) -> number.toShort()
            targetcl == Byte::class.javaPrimitiveType || Byte::class.javaObjectType.isAssignableFrom(targetcl) -> number.toByte()
            targetcl == Float::class.javaPrimitiveType || Float::class.javaObjectType.isAssignableFrom(targetcl) -> number.toFloat()
            targetcl == Double::class.javaPrimitiveType || Double::class.javaObjectType.isAssignableFrom(targetcl) -> number.toDouble()
            else -> number
        }
    }

    /**
     * Calls the event always asynchronously. The return value is always false if async.
     *
     * @param event The event to call
     * @return The event cancelled state or false if async.
     */
    @JvmStatic
    fun <T> callEventAsync(event: T): Boolean where T : Event, T : Cancellable {
        val task = Supplier {
            Bukkit.getPluginManager().callEvent(event)
            event.isCancelled
        }
        return doItAsync(task, false)
    }

    /**
     * Does something always asynchronously. It will execute in the same thread if it's not the server thread.
     *
     * @param what What to do
     * @param def  Default if async
     * @return The value supplied by the action or def if async.
     */
    @JvmStatic
    fun <T> doItAsync(what: Supplier<T>, def: T): T {
        return if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.instance, Runnable { what.get() })
            def
        } else what.get()
    }

    /**
     * Log a warning message if the plugin is initialized. If not, just print a regular message.
     */
    fun logWarn(message: String) {
        if (MainPlugin.isInitialized) {
            MainPlugin.instance.logger.warning(message)
        } else {
            println(message)
        }
    }

    /**
     * Returns true while unit testing.
     */
    @JvmStatic
    var isTest = false

    interface IHaveFancyName {
        /**
         * May not be null.
         *
         * @return The name to be displayed in most places.
         */
        val fancyName: String

        /**
         * May return null.
         *
         * @return The full name that can be used to uniquely identify the user.
         */
        val fancyFullName: String
    }
}