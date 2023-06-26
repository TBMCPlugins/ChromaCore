package buttondevteam.lib.architecture

import buttondevteam.core.MainPlugin
import buttondevteam.lib.ChromaUtils
import buttondevteam.lib.architecture.config.IConfigData
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.scheduler.BukkitTask
import java.util.function.Function

/**
 * Use the getter/setter constructor if [T] isn't a primitive type or String.
 *
 *  Use [Component.config] or [ButtonPlugin.iConfig] then [IHaveConfig.getData] to get an instance.
 *
 * **Note:** The instance can become outdated if the config is reloaded.
 * @param config The config object to use for the whole file
 * @param path The path to the config value
 * @param primitiveDef The default value, as stored in the config. Non-nullable as it needs to be saved sto the config
 * @param getter Function to convert primtive types to [T]. The parameter is of a primitive type as returned by [Configuration.get]
 * @param setter Function to convert [T] to a primitive type. The result should be a primitive type or string that can be retrieved correctly later
 * @param readOnly If true, changing the value will have no effect
 * @param T The type of the config value. May be nullable if the getter cannot always return a value
 */
class ConfigData<T : Any?> internal constructor(
    val config: IHaveConfig,
    override val path: String,
    private val primitiveDef: Any,
    private val getter: Function<Any, T>,
    private val setter: Function<T, Any>,
    private val readOnly: Boolean
) : IConfigData<T> {

    /**
     * The config value should not change outside this instance
     */
    private var value: T? = null

    init {
        get() //Generate config automatically
    }

    override fun toString(): String {
        return "ConfigData{path='$path', value=$value}"
    }

    override fun get(): T {
        val cachedValue = value
        if (cachedValue != null) return cachedValue //Speed things up
        val config = config.config
        val freshValue = config?.get(path) ?: primitiveDef.also { setInternal(it) }
        return getter.apply(convertPrimitiveType(freshValue)).also { value = it }
    }

    override fun reload() {
        value = null
    }

    /**
     * Converts a value to [T] from the representation returned by [Configuration.get].
     */
    private fun convertPrimitiveType(value: Any): Any {
        return if (primitiveDef is Number) //If we expect a number
            if (value is Number) ChromaUtils.convertNumber(value, primitiveDef.javaClass)
            else primitiveDef //If we didn't get a number, return default (which is a number)
        else if (value is List<*> && primitiveDef.javaClass.isArray) // If we got a list and we expected an array
            value.toTypedArray<Any?>()
        else value
    }

    override fun set(value: T) {
        if (readOnly) return  //Safety for Discord channel/role data
        val `val` = setter.apply(value)
        setInternal(`val`)
        this.value = value
    }

    private fun setInternal(`val`: Any?) {
        val config = config.config
        if (config != null) {
            config.set(path, `val`)
            signalChange(this.config)
        } else {
            ChromaUtils.logWarn("Attempted to get/set config value with no config! Path: $path, value: $`val`")
        }
    }

    /**
     * @param task The running task, if it was scheduled
     */
    private class SaveTask(val task: BukkitTask?, val saveAction: Runnable)

    companion object {
        private val saveTasks = HashMap<Configuration, SaveTask>()

        /**
         * Signals that the config has changed and should be saved
         */
        fun signalChange(config: IHaveConfig) {
            val cc = config.config
            val sa = config.saveAction
            val root = cc?.root
            if (root == null) {
                ChromaUtils.logWarn("Attempted to save config with no root! Name: ${cc?.name ?: "NONEXISTENT CONFIG"}")
                return
            }
            if (!MainPlugin.isInitialized) {
                // If the plugin isn't initialized, we can't schedule a task - do it when the plugin is enabled
                synchronized(saveTasks) {
                    saveTasks.put(root, SaveTask(null, sa))
                }
            } else if (!saveTasks.containsKey(cc.root)) {
                synchronized(saveTasks) {
                    saveTasks.put(
                        root,
                        SaveTask(Bukkit.getScheduler().runTaskLaterAsynchronously(MainPlugin.instance, Runnable {
                            synchronized(saveTasks) {
                                saveTasks.remove(root)
                                sa.run()
                            }
                        }, 100), sa)
                    )
                }
            }
        }

        /**
         * Saves the config immediately, if it's scheduled to be saved. Used to save configs when the plugin is disabled or reloaded.
         *
         * Also performs cleanup of the save task, so it must be called when the ConfigData is invalidated (which is the above two cases).
         */
        @JvmStatic
        fun saveNow(config: Configuration): Boolean {
            synchronized(saveTasks) {
                val st = saveTasks[config]
                if (st != null) {
                    st.task?.cancel()
                    saveTasks.remove(config)
                    st.saveAction.run()
                    return true
                }
            }
            return false
        }
    }
}