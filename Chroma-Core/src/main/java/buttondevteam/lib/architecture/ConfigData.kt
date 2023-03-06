package buttondevteam.lib.architecture

import buttondevteam.core.MainPlugin
import buttondevteam.lib.ChromaUtils
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.scheduler.BukkitTask
import java.util.function.Function

/**
 * Use the getter/setter constructor if [T] isn't a primitive type or String.<br></br>
 * Use [Component.getConfig] or [ButtonPlugin.getIConfig] then [IHaveConfig.getData] to get an instance.
 * @param config May be null for testing
 * @param getter The parameter is of a primitive type as returned by [Configuration.get]
 * @param setter The result should be a primitive type or string that can be retrieved correctly later
 */
open class ConfigData<T> internal constructor(
    private val config: IHaveConfig?,
    val path: String,
    def: T?,
    primitiveDef: Any?,
    private val getter: Function<Any?, T>,
    private val setter: Function<T, Any?>
) {
    private val pdef: Any?

    /**
     * The config value should not change outside this instance
     */
    private var value: T? = null

    init {
        this.pdef = primitiveDef ?: def?.let { setter.apply(it) }
            ?: throw IllegalArgumentException("Either def or primitiveDef must be set. A getter and setter must be present when using primitiveDef.")
        get() //Generate config automatically
    }

    override fun toString(): String {
        return "ConfigData{path='$path', value=$value}"
    }

    fun reset() {
        value = null
    }

    fun get(): T? {
        if (value != null) return value //Speed things up
        val config = config?.config
        var `val`: Any?
        if (config == null || !config.isSet(path)) {
            `val` = pdef
            setInternal(pdef) // Save default value even if read-only
        } else `val` = config.get(path) //config==null: testing
        if (`val` == null) //If it's set to null explicitly
            `val` = pdef
        fun convert(_val: Any?, _pdef: Any?): Any? {
            return if (_pdef is Number) //If we expect a number
                if (_val is Number)
                    ChromaUtils.convertNumber(_val as Number?, _pdef.javaClass as Class<out Number?>)
                else _pdef //If we didn't get a number, return default (which is a number)
            else if (_val is List<*> && _pdef != null && _pdef.javaClass.isArray)
                _val.toTypedArray()
            else _val
        }
        return getter.apply(convert(`val`, pdef)).also { value = it }
    }

    fun set(value: T?) {
        if (this is ReadOnlyConfigData<*>) return  //Safety for Discord channel/role data
        val `val` = value?.let { setter.apply(value) }
        setInternal(`val`)
        this.value = value
    }

    private fun setInternal(`val`: Any?) {
        if (config == null) return
        config.config.set(path, `val`)
        signalChange(config)
    }

    private class SaveTask(val task: BukkitTask, val saveAction: Runnable)

    class ConfigDataBuilder<T> internal constructor(private val config: IHaveConfig, private val path: String) {
        private var def: T? = null
        private var primitiveDef: Any? = null

        @Suppress("UNCHECKED_CAST")
        private var getter: Function<Any?, T> = Function { it as T }
        private var setter: Function<T, Any?> = Function { it }

        /**
         * The default value to use, as used in code. If not a primitive type, use the [.getter] and [.setter] methods.
         * <br></br>
         * To set the value as it is stored, use [.primitiveDef].
         *
         * @param def The default value
         * @return This builder
         */
        fun def(def: T): ConfigDataBuilder<T> {
            this.def = def
            return this
        }

        /**
         * The default value to use, as stored in yaml. Must be a primitive type. Make sure to use the [.getter] and [.setter] methods.
         * <br></br>
         * To set the value as used in the code, use [.def].
         *
         * @param primitiveDef The default value
         * @return This builder
         */
        fun primitiveDef(primitiveDef: Any?): ConfigDataBuilder<T> {
            this.primitiveDef = primitiveDef
            return this
        }

        /**
         * A function to use to obtain the runtime object from the yaml representation (usually string).
         * The [.setter] must also be set.
         *
         * @param getter A function that receives the primitive type and returns the runtime type
         * @return This builder
         */
        fun getter(getter: Function<Any?, T>): ConfigDataBuilder<T> {
            this.getter = getter
            return this
        }

        /**
         * A function to use to obtain the yaml representation (usually string) from the runtime object.
         * The [.getter] must also be set.
         *
         * @param setter A function that receives the runtime type and returns the primitive type
         * @return This builder
         */
        fun setter(setter: Function<T, Any?>): ConfigDataBuilder<T> {
            this.setter = setter
            return this
        }

        /**
         * Builds a modifiable config representation. Use if you want to change the value *in code*.
         *
         * @return A ConfigData instance.
         */
        fun build(): ConfigData<T> {
            val config = ConfigData(config, path, def, primitiveDef, getter, setter)
            this.config.onConfigBuild(config)
            return config
        }

        /**
         * Builds a read-only config representation. Use if you only want the value to be changed *in the config*.
         *
         * @return A ReadOnlyConfigData instance.
         */
        fun buildReadOnly(): ReadOnlyConfigData<T> {
            val config = ReadOnlyConfigData(config, path, def, primitiveDef, getter, setter)
            this.config.onConfigBuild(config)
            return config
        }

        override fun toString(): String {
            return "ConfigData.ConfigDataBuilder(config=$config, path=$path, def=$def, primitiveDef=$primitiveDef, getter=$getter, setter=$setter)"
        }
    }

    companion object {
        private val saveTasks = HashMap<Configuration, SaveTask>()
        fun signalChange(config: IHaveConfig) {
            val cc = config.config
            val sa = config.saveAction
            if (!saveTasks.containsKey(cc.root)) {
                synchronized(saveTasks) {
                    saveTasks.put(
                        cc.root,
                        SaveTask(Bukkit.getScheduler().runTaskLaterAsynchronously(MainPlugin.Instance, {
                            synchronized(
                                saveTasks
                            ) {
                                saveTasks.remove(cc.getRoot())
                                sa!!.run()
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

        fun <T> builder(config: IHaveConfig, path: String): ConfigDataBuilder<T> {
            return ConfigDataBuilder(config, path)
        }
    }
}