package buttondevteam.lib

import com.google.common.collect.Lists
import org.bukkit.event.Event
import org.bukkit.event.EventException
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.*

internal abstract class EventExceptionHandler {
    // https://gist.github.com/aadnk/5430459
    // For wrapping a registered listener
    private class ExceptionRegisteredListener(private val delegate: RegisteredListener, private val handler: EventExceptionHandler) : RegisteredListener(
        delegate.listener, NULL_EXECUTOR, delegate.priority,
        delegate.plugin, delegate.isIgnoringCancelled
    ) {
        @Throws(EventException::class)
        override fun callEvent(event: Event) {
            try {
                delegate.callEvent(event)
            } catch (e: EventException) {
                if (!handler.handle(e.cause ?: e, event)) {
                    throw e
                }
            } catch (e: Throwable) {
                if (!handler.handle(e, event)) {
                    throw e
                }
            }
        }

        companion object {
            /**
             * Represents an event executor that does nothing. This is not really necessary in the current
             * implementation of CraftBukkit, but we will take no chances.
             */
            private val NULL_EXECUTOR = EventExecutor { _, _ -> }
        }
    }

    /**
     * Handle a given exception.
     * @param ex - the exception to handle.
     * @param event - the event that was being handled.
     * @return TRUE to indicate that the exception has been handled, FALSE to rethrow it.
     */
    abstract fun handle(ex: Throwable, event: Event): Boolean

    companion object {
        /**
         * Register Bukkit event handlers with a given exception handler.
         * @param listener - a class of event handlers.
         * @param plugin - the current plugin.
         * @param handler - exception handler.
         */
        fun registerEvents(listener: Listener, plugin: Plugin, handler: EventExceptionHandler) {
            registerEvents(plugin.server.pluginManager, listener, plugin, handler)
        }

        /**
         * Register Bukkit event handlers with a given exception handler.
         * @param manager - the current plugin manager.
         * @param listener - a class of event handlers.
         * @param plugin - the current plugin.
         * @param handler - exception handler.
         */
        fun registerEvents(manager: PluginManager, listener: Listener, plugin: Plugin, handler: EventExceptionHandler) {
            if (!plugin.isEnabled) {
                throw IllegalPluginAccessException("Plugin attempted to register $listener while not enabled")
            }


            // Create normal listeners
            for (entry in plugin.pluginLoader.createRegisteredListeners(listener, plugin).entries) {
                // Wrap these listeners in our exception handler

                getHandlerList(entry.key)!!.registerAll(wrapAll(entry.value, handler!!))
            }
        }

        /**
         * Wrap every listener in the given collection around an exception handler.
         * @param listeners - the listeners to wrap.
         * @param handler - the exception handler to add.
         * @return The wrapped listeners.
         */
        private fun wrapAll(listeners: MutableCollection<RegisteredListener>, handler: EventExceptionHandler): MutableCollection<RegisteredListener?> {
            val output: MutableList<RegisteredListener?> = Lists.newArrayList<RegisteredListener?>()

            for (listener in listeners) {
                output.add(ExceptionRegisteredListener(listener, handler))
            }
            return output
        }

        /**
         * Retrieve the handler list associated with the given class.
         * @param clazz - given event class.
         * @return Associated handler list.
         */
        private fun getHandlerList(clazz: Class<out Event?>): HandlerList? {
            // Class must have Event as its superclass
            var clazz = clazz
            while (clazz.getSuperclass() != null && Event::class.java.isAssignableFrom(clazz.getSuperclass())) {
                try {
                    val method = clazz.getDeclaredMethod("getHandlerList")
                    method.setAccessible(true)
                    return method.invoke(null) as HandlerList?
                } catch (_: NoSuchMethodException) {
                    // Keep on searching
                    clazz = clazz.getSuperclass().asSubclass(Event::class.java)
                } catch (e: Exception) {
                    throw IllegalPluginAccessException(e.message)
                }
            }
            throw IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName())
        }
    }
}