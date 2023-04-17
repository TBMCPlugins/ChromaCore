package buttondevteam.lib.architecture

import buttondevteam.core.MainPlugin
import buttondevteam.lib.ChromaUtils
import buttondevteam.lib.architecture.config.IConfigData
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.scheduler.BukkitTask
import java.util.function.Function

/**
 * Use the getter/setter constructor if [T] isn't a primitive type or String.<br></br>
 * Use [Component.config] or [ButtonPlugin.iConfig] then [IHaveConfig.getData] to get an instance.
 * @param config May be null for testing
 * @param getter The parameter is of a primitive type as returned by [Configuration.get]
 * @param setter The result should be a primitive type or string that can be retrieved correctly later
 */
class ConfigData<T> internal constructor(
    val config: IHaveConfig?,
    override val path: String,
    primitiveDef: Any?,
    private val getter: Function<Any?, T>,
    private val setter: Function<T, Any?>,
    private val readOnly: Boolean
) : IConfigData<T> {
    private val pdef: Any?

    /**
     * The config value should not change outside this instance
     */
    private var value: T? = null

    init {
        this.pdef = primitiveDef
            ?: throw IllegalArgumentException("Either def or primitiveDef must be set. A getter and setter must be present when using primitiveDef.")
        get() //Generate config automatically
    }

    override fun toString(): String {
        return "ConfigData{path='$path', value=$value}"
    }

    override fun get(): T {
        val cachedValue = value
        if (cachedValue != null) return cachedValue //Speed things up
        val config = config?.config
        var `val`: Any?
        if (config == null || !config.isSet(path)) {
            `val` = pdef
            setInternal(pdef) // Save default value even if read-only
        } else `val` = config.get(path) //config==null: testing
        if (`val` == null) //If it's set to null explicitly
            `val` = pdef
        fun convert(cval: Any?, cpdef: Any?): Any? {
            return if (cpdef is Number) //If we expect a number
                if (cval is Number)
                    ChromaUtils.convertNumber(cval, cpdef.javaClass)
                else cpdef //If we didn't get a number, return default (which is a number)
            else if (cval is List<*> && cpdef != null && cpdef.javaClass.isArray)
                cval.toTypedArray()
            else cval
        }
        return getter.apply(convert(`val`, pdef)).also { value = it }
    }

    override fun set(value: T?) { // TODO: Have a separate method for removing the value from the config and make this non-nullable
        if (readOnly) return  //Safety for Discord channel/role data
        val `val` = value?.let { setter.apply(it) }
        setInternal(`val`)
        this.value = value
    }

    private fun setInternal(`val`: Any?) {
        if (config == null) return
        config.config.set(path, `val`)
        signalChange(config)
    }

    private class SaveTask(val task: BukkitTask, val saveAction: Runnable)

    companion object {
        private val saveTasks = HashMap<Configuration, SaveTask>()
        fun signalChange(config: IHaveConfig) {
            val cc = config.config
            val sa = config.saveAction
            val root = cc.root
            if (root == null) {
                MainPlugin.instance.logger.warning("Attempted to save config with no root! Name: ${config.config.name}")
                return
            }
            if (!saveTasks.containsKey(cc.root)) {
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

        @JvmStatic
        fun saveNow(config: Configuration): Boolean {
            synchronized(saveTasks) {
                val st = saveTasks[config]
                if (st != null) {
                    st.task.cancel()
                    saveTasks.remove(config)
                    st.saveAction.run()
                    return true
                }
            }
            return false
        }
    }
}