package buttondevteam.lib

import buttondevteam.core.MainPlugin
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.ChromaGamerBase.Companion.registerPluginUserClass
import buttondevteam.lib.potato.DebugPotato
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

object TBMCCoreAPI {
    val coders: List<String> = listOf("Alisolarflare", "NorbiPeti", "iie", "thewindmillman", "mayskam1995")

    @JvmStatic
    @Throws(IOException::class)
    fun DownloadString(urlstr: String?): String {
        val url = URL(urlstr)
        val con = url.openConnection()
        con.setRequestProperty("User-Agent", "TBMCPlugins")
        val `in` = con.getInputStream()
        var encoding = con.contentEncoding
        encoding = encoding ?: "UTF-8"
        val s = Scanner(`in`).useDelimiter("\\A")
        val body = if (s.hasNext()) s.next() else ""
        `in`.close()
        return body
    }

    private val exceptionsToSend = HashMap<String, Throwable>()
    private val debugMessagesToSend: MutableList<String> = ArrayList()

    /**
     * Send exception to the [TBMCExceptionEvent].
     *
     * @param sourcemsg A message that is shown at the top of the exception (before the exception's message)
     * @param e         The exception to send
     */
    @JvmStatic
    fun SendException(sourcemsg: String, e: Throwable, component: Component<*>) {
        SendException(sourcemsg, e, false) { message: String? -> component.logWarn(message!!) }
    }

    /**
     * Send exception to the [TBMCExceptionEvent].
     *
     * @param sourcemsg A message that is shown at the top of the exception (before the exception's message)
     * @param e         The exception to send
     */
    @JvmStatic
    fun SendException(sourcemsg: String, e: Throwable, plugin: JavaPlugin) {
        SendException(sourcemsg, e, false) { msg: String? -> plugin.logger.warning(msg) }
    }

    @JvmStatic
    fun SendException(sourcemsg: String, e: Throwable, debugPotato: Boolean, logWarn: Consumer<String?>) {
        try {
            SendUnsentExceptions()
            val event = TBMCExceptionEvent(sourcemsg, e)
            Bukkit.getPluginManager().callEvent(event)
            synchronized(exceptionsToSend) { if (!event.isHandled) exceptionsToSend[sourcemsg] = e }
            logWarn.accept(sourcemsg)
            e.printStackTrace()
            if (debugPotato) {
                val devsOnline: MutableList<Player> = ArrayList()
                for (player in Bukkit.getOnlinePlayers()) {
                    if (coders.contains(player.name)) {
                        devsOnline.add(player)
                    }
                }
                if (devsOnline.isNotEmpty()) {
                    val potato = DebugPotato()
                        .setMessage(
                            arrayOf( //
                                "${ChatColor.AQUA}${ChatColor.ITALIC}" + e.javaClass.simpleName,  //
                                "${ChatColor.RED}${ChatColor.ITALIC}$sourcemsg",  //
                                "${ChatColor.GREEN}${ChatColor.ITALIC}Find a dev to fix this issue"
                            )
                        )
                        .setType(
                            when (e) {
                                is IOException -> "Throwable Potato"
                                is ClassCastException -> "Squished Potato"
                                is NullPointerException -> "Plain Potato"
                                is StackOverflowError -> "Chips"
                                else -> "Error Potato"
                            }
                        )
                    for (dev in devsOnline) {
                        potato.Send(dev)
                    }
                }
            }
        } catch (ee: Exception) {
            System.err.println("Failed to send exception!")
            ee.printStackTrace()
        }
    }

    @JvmStatic
    fun sendDebugMessage(debugMessage: String) {
        SendUnsentDebugMessages()
        val event = TBMCDebugMessageEvent(debugMessage)
        Bukkit.getPluginManager().callEvent(event)
        synchronized(debugMessagesToSend) { if (!event.isSent) debugMessagesToSend.add(debugMessage) }
    }

    private var eventExceptionCoreHandler: EventExceptionCoreHandler? = null

    /**
     * Registers Bukkit events, handling the exceptions occurring in those events
     *
     * @param listener The class that handles the events
     * @param plugin   The plugin which the listener belongs to
     */
    @JvmStatic
    fun RegisterEventsForExceptions(listener: Listener, plugin: Plugin) {
        if (eventExceptionCoreHandler == null) eventExceptionCoreHandler = EventExceptionCoreHandler()
        EventExceptionHandler.registerEvents(listener, plugin, eventExceptionCoreHandler)
    }

    @JvmStatic
    fun <T : ChromaGamerBase> RegisterUserClass(userclass: Class<T>, constructor: Supplier<T>) {
        registerPluginUserClass(userclass, constructor)
    }

    /**
     * Send exceptions that haven't been sent (their events didn't get handled). This method is used by the DiscordPlugin's ready event
     */
    @JvmStatic
    fun SendUnsentExceptions() {
        synchronized(exceptionsToSend) {
            if (exceptionsToSend.size > 20) {
                exceptionsToSend.clear() // Don't call more and more events if all the handler plugins are unloaded
                Bukkit.getLogger().warning("Unhandled exception list is over 20! Clearing!")
            }
            val iterator: MutableIterator<Map.Entry<String, Throwable>> = exceptionsToSend.entries.iterator()
            while (iterator.hasNext()) {
                val (key, value) = iterator.next()
                val event = TBMCExceptionEvent(key, value)
                Bukkit.getPluginManager().callEvent(event)
                if (event.isHandled) iterator.remove()
            }
        }
    }

    @JvmStatic
    fun SendUnsentDebugMessages() {
        synchronized(debugMessagesToSend) {
            if (debugMessagesToSend.size > 20) {
                debugMessagesToSend.clear() // Don't call more and more DebugMessages if all the handler plugins are unloaded
                Bukkit.getLogger().warning("Unhandled Debug Message list is over 20! Clearing!")
            }
            val iterator = debugMessagesToSend.iterator()
            while (iterator.hasNext()) {
                val message = iterator.next()
                val event = TBMCDebugMessageEvent(message)
                Bukkit.getPluginManager().callEvent(event)
                if (event.isSent) iterator.remove()
            }
        }
    }

    /**
     * Returns true if the server is a test/dev/staging server
     */
    @JvmStatic
    fun IsTestServer(): Boolean {
        return MainPlugin.instance.test.get()
    }
}