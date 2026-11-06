package buttondevteam.lib

import buttondevteam.core.MainPlugin
import buttondevteam.lib.test.TestException
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import java.util.function.Supplier
import kotlin.reflect.KClass

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
    fun convertNumber(number: Number, targetcl: KClass<out Number>): Number {
        return when (targetcl) {
            Long::class -> number.toLong()
            Int::class -> number.toInt() //Needed because the parser can get longs
            Short::class -> number.toShort()
            Byte::class -> number.toByte()
            Float::class -> number.toFloat()
            Double::class -> number.toDouble()
            else -> number
        }
    }

    @JvmStatic
    fun convertArray(obj: List<*>, primitiveDef: Array<*>): Any {
        return when {
            primitiveDef.isArrayOf<Long>() -> obj.map { it as Long }.toLongArray()
            primitiveDef.isArrayOf<Int>() -> obj.map { it as Int }.toIntArray()
            primitiveDef.isArrayOf<Short>() -> obj.map { it as Short }.toShortArray()
            primitiveDef.isArrayOf<Byte>() -> obj.map { it as Byte }.toByteArray()
            primitiveDef.isArrayOf<Float>() -> obj.map { it as Float }.toFloatArray()
            primitiveDef.isArrayOf<Double>() -> obj.map { it as Double }.toDoubleArray()
            else -> obj.toTypedArray()
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
     * Throws the exception directly during testing but only reports it when running on the server.
     * This way exceptions get reported properly when running, but they can be checked during testing.
     *
     * Useful in code blocks scheduled using the Bukkit API.
     */
    fun throwWhenTested(exception: Throwable, message: String) {
        if (isTest) {
            // Propagate exception back to the tests
            throw TestException(message, exception)
        } else {
            // Otherwise we don't run the code directly, so we need to handle this here
            TBMCCoreAPI.SendException(message, exception, MainPlugin.instance)
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