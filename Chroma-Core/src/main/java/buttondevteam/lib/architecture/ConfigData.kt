package buttondevteam.lib.architecture

import buttondevteam.core.MainPlugin
import buttondevteam.lib.ChromaUtils
import buttondevteam.lib.architecture.IHaveConfig.getConfig
import lombok.*
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.scheduler.BukkitTask
import java.util.function.BiFunction
import java.util.function.Function

/**
 * Use the getter/setter constructor if [T] isn't a primitive type or String.<br></br>
 * Use [Component.getConfig] or [ButtonPlugin.getIConfig] then [IHaveConfig.getData] to get an instance.
 */
open class ConfigData<T> internal constructor(
    config: IHaveConfig?,
    path: String?,
    def: T,
    primitiveDef: Any?,
    getter: Function<Any?, T>?,
    setter: Function<T, Any?>?
) {
    /**
     * May be null for testing
     */
    private val config: IHaveConfig?

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private val path: String?
    protected val def: T?
    private val primitiveDef: Any?

    /**
     * The parameter is of a primitive type as returned by [YamlConfiguration.get]
     */
    private val getter: Function<Any?, T>?

    /**
     * The result should be a primitive type or string that can be retrieved correctly later
     */
    private val setter: Function<T, Any?>?

    /**
     * The config value should not change outside this instance
     */
    private var value: T? = null

    init {
        var def: T? = def
        var primitiveDef = primitiveDef
        if (def == null) {
            requireNotNull(primitiveDef) { "Either def or primitiveDef must be set." }
            requireNotNull(getter) { "A getter and setter must be present when using primitiveDef." }
            def = getter.apply(primitiveDef)
        } else if (primitiveDef == null) primitiveDef = if (setter == null) def else setter.apply(def)
        require(getter == null == (setter == null)) { "Both setters and getters must be present (or none if def is primitive)." }
        this.config = config
        this.path = path
        this.def = def
        this.primitiveDef = primitiveDef
        this.getter = getter
        this.setter = setter
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
        val config = config!!.getConfig<Any>()
        var `val`: Any?
        if (config == null || !config.isSet(path)) { //Call set() if config == null
            `val` = primitiveDef
            if ((def == null || this is ReadOnlyConfigData<*>) && config != null) //In Discord's case def may be null
                setInternal(primitiveDef) //If read-only then we still need to save the default value so it can be set
            else set(def) //Save default value - def is always set
        } else `val` = config.get(path) //config==null: testing
        if (`val` == null) //If it's set to null explicitly
            `val` = primitiveDef
        val convert = BiFunction { _val: Any?, _def: Any? ->
            if (_def is Number) //If we expect a number
                _val = if (_val is Number) ChromaUtils.convertNumber(
                    _val as Number?,
                    _def.javaClass as Class<out Number?>
                ) else _def //If we didn't get a number, return default (which is a number)
            else if (_val is List<*> && _def != null && _def.javaClass.isArray) _val = (_val as List<T>).toArray<T>(
                java.lang.reflect.Array.newInstance(
                    _def.javaClass.componentType,
                    0
                ) as Array<T>
            )
            _val
        }
        if (getter != null) {
            `val` = convert.apply(`val`, primitiveDef)
            var hmm: T? = getter.apply(`val`)
            if (hmm == null) hmm = def //Set if the getter returned null
            return hmm
        }
        `val` = convert.apply(`val`, def)
        return `val` as T?. also {
            value = it //Always cache, if not cached yet
        }
    }

    fun set(value: T?) {
        if (this is ReadOnlyConfigData<*>) return  //Safety for Discord channel/role data
        val `val`: Any?
        `val` = if (setter != null && value != null) setter.apply(value) else value
        if (config!!.getConfig<Any>() != null) setInternal(`val`)
        this.value = value
    }

    private fun setInternal(`val`: Any?) {
        config!!.getConfig<Any>().set(path, `val`)
        signalChange(config)
    }

    @AllArgsConstructor
    private class SaveTask {
        var task: BukkitTask? = null
        var saveAction: Runnable? = null
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    class ConfigDataBuilder<T> {
        private val config: IHaveConfig? = null
        private val path: String? = null
        private var def: T? = null
        private var primitiveDef: Any? = null
        private var getter: Function<Any?, T?>? = null
        private var setter: Function<T?, Any?>? = null

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
        fun getter(getter: Function<Any?, T>?): ConfigDataBuilder<T> {
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
        fun setter(setter: Function<T, Any?>?): ConfigDataBuilder<T> {
            this.setter = setter
            return this
        }

        /**
         * Builds a modifiable config representation. Use if you want to change the value *in code*.
         *
         * @return A ConfigData instance.
         */
        fun build(): ConfigData<T?> {
            val config = ConfigData(config, path, def, primitiveDef, getter, setter)
            this.config!!.onConfigBuild(config)
            return config
        }

        /**
         * Builds a read-only config representation. Use if you only want the value to be changed *in the config*.
         *
         * @return A ReadOnlyConfigData instance.
         */
        fun buildReadOnly(): ReadOnlyConfigData<T?> {
            val config = ReadOnlyConfigData(config, path, def, primitiveDef, getter, setter)
            this.config!!.onConfigBuild(config)
            return config
        }

        override fun toString(): String {
            return "ConfigData.ConfigDataBuilder(config=" + config + ", path=" + path + ", def=" + def + ", primitiveDef=" + primitiveDef + ", getter=" + getter + ", setter=" + setter + ")"
        }
    }

    companion object {
        private val saveTasks = HashMap<Configuration, SaveTask>()
        fun signalChange(config: IHaveConfig?) {
            val cc = config!!.getConfig<Any>()
            val sa = config.saveAction
            if (!saveTasks.containsKey(cc.getRoot())) {
                synchronized(saveTasks) {
                    saveTasks.put(
                        cc.getRoot(),
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
                    st.task!!.cancel()
                    saveTasks.remove(config)
                    st.saveAction!!.run()
                    return true
                }
            }
            return false
        }

        fun <T> builder(config: IHaveConfig?, path: String?): ConfigDataBuilder<T> {
            return ConfigDataBuilder(config, path)
        }
    }
}