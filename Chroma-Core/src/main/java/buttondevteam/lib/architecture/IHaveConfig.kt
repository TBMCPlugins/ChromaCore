package buttondevteam.lib.architecture

import buttondevteam.core.MainPlugin
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.ConfigData.ConfigDataBuilder
import lombok.Getter
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Collectors

/**
 * A config system
 * May be used in testing.
 *
 * @param saveAction What to do to save the config to disk. Don't use get methods until it's non-null.
 */
class IHaveConfig(var saveAction: Runnable?) { // TODO: Make non-nullable after adding component builder
    private val datamap = HashMap<String, ConfigData<*>>()

    /**
     * Returns the Bukkit ConfigurationSection. Use [.signalChange] after changing it.
     */
    @Getter
    private var config: ConfigurationSection? = null

    /**
     * Gets a config object for the given path. The def or primitiveDef must be set. If a getter is present, a setter must be present as well.
     *
     * @param path The dot-separated path relative to this config instance
     * @param <T>  The runtime type of the config value
     * @return A ConfigData builder to set how to obtain the value
    </T> */
    fun <T> getConfig(path: String?): ConfigDataBuilder<T> {
        return ConfigData.builder(this, path)
    }

    fun onConfigBuild(config: ConfigData<*>) {
        datamap[config.path] = config
    }

    /**
     * This method overload should only be used with primitives or String.
     *
     * @param path The path in config to use
     * @param def  The value to use by default
     * @param <T>  The type of this variable (only use primitives or String)
     * @return The data object that can be used to get or set the value
    </T> */
    fun <T> getData(path: String, def: T): ConfigData<T> {
        var data = datamap[path]
        if (data == null) datamap[path] = ConfigData(this, path, def, def, null, null).also { data = it }
        @Suppress("UNCHECKED_CAST")
        return data as ConfigData<T>
    }

    /**
     * This method overload may be used with any class.
     *
     * @param path   The path in config to use
     * @param def    The value to use by default
     * @param getter A function that converts a primitive representation to the correct value
     * @param setter A function that converts a value to a primitive representation
     * @param <T>    The type of this variable (can be any class)
     * @return The data object that can be used to get or set the value
    </T> */
    fun <T> getData(path: String, def: T, getter: Function<Any?, T>?, setter: Function<T, Any?>): ConfigData<T> {
        var data = datamap[path]
        if (data == null) datamap[path] =
            ConfigData(this, path, def, setter.apply(def), getter, setter).also { data = it }
        @Suppress("UNCHECKED_CAST")
        return data as ConfigData<T>
    }

    /**
     * This method overload may be used with any class. The given default value will be run through the getter.
     *
     * @param path         The path in config to use
     * @param primitiveDef The **primitive** value to use by default
     * @param getter       A function that converts a primitive representation to the correct value
     * @param setter       A function that converts a value to a primitive representation
     * @param <T>          The type of this variable (can be any class)
     * @return The data object that can be used to get or set the value
    </T> */
    fun <T> getDataPrimDef(
        path: String,
        primitiveDef: Any?,
        getter: Function<Any?, T>,
        setter: Function<T, Any?>?
    ): ConfigData<T> {
        var data = datamap[path]
        if (data == null) datamap[path] =
            ConfigData(this, path, getter.apply(primitiveDef), primitiveDef, getter, setter).also { data = it }
        @Suppress("UNCHECKED_CAST")
        return data as ConfigData<T>
    }

    /**
     * This method overload may be used with any class. The given default value will be run through the getter.
     *
     * @param path         The path in config to use
     * @param primitiveDef The **primitive** value to use by default
     * @param getter       A function that converts a primitive representation to the correct value
     * @param setter       A function that converts a value to a primitive representation
     * @param <T>          The type of this variable (can be any class)
     * @return The data object that can be used to get or set the value
    </T> */
    fun <T> getReadOnlyDataPrimDef(
        path: String,
        primitiveDef: Any?,
        getter: Function<Any?, T>,
        setter: Function<T, Any?>?
    ): ReadOnlyConfigData<T> {
        var data = datamap[path]
        if (data == null) datamap[path] =
            ReadOnlyConfigData(this, path, getter.apply(primitiveDef), primitiveDef, getter, setter).also { data = it }
        @Suppress("UNCHECKED_CAST")
        return data as ReadOnlyConfigData<T>
    }

    /**
     * This method overload should only be used with primitves or String.
     *
     * @param path The path in config to use
     * @param def  The value to use by default
     * @param <T>  The type of this variable (only use primitives or String)
     * @return The data object that can be used to get or set the value
    </T> */
    fun <T> getData(path: String, def: Supplier<T>): ConfigData<T> {
        var data = datamap[path]
        if (data == null) {
            val defval = def.get()
            datamap[path] = ConfigData(this, path, defval, defval, null, null).also { data = it }
        }
        @Suppress("UNCHECKED_CAST")
        return data as ConfigData<T>
    }

    /**
     * This method overload may be used with any class.
     *
     * @param path   The path in config to use
     * @param def    The value to use by default
     * @param getter A function that converts a primitive representation to the correct value
     * @param setter A function that converts a value to a primitive representation
     * @param <T>    The type of this variable (can be any class)
     * @return The data object that can be used to get or set the value
    </T> */
    fun <T> getData(
        path: String,
        def: Supplier<T>,
        getter: Function<Any?, T>?,
        setter: Function<T, Any?>
    ): ConfigData<T> {
        var data = datamap[path]
        if (data == null) {
            val defval = def.get()
            datamap[path] = ConfigData(this, path, defval, setter.apply(defval), getter, setter).also { data = it }
        }
        @Suppress("UNCHECKED_CAST")
        return data as ConfigData<T>
    }

    /**
     * This method overload should only be used with primitves or String.
     *
     * @param path The path in config to use
     * @param <T>  The type of this variable (only use primitives or String)
     * @return The data object that can be used to get or set the value
    </T> */
    fun <T> getListData(path: String): ListConfigData<T> {
        var data = datamap[path]
        if (data == null) datamap[path] = ListConfigData(this, path, ListConfigData.List<T>()).also { data = it }
        @Suppress("UNCHECKED_CAST")
        return data as ListConfigData<T>
    }

    /**
     * Schedules a save operation. Use after changing the ConfigurationSection directly.
     */
    fun signalChange() {
        ConfigData.signalChange(this)
    }

    /**
     * Clears all caches and loads everything from yaml.
     */
    fun reset(config: ConfigurationSection?) {
        this.config = config
        datamap.forEach { (path: String?, data: ConfigData<*>) -> data.reset() }
    }

    companion object {
        /**
         * Generates the config YAML.
         *
         * @param obj       The object which has config methods
         * @param configMap The result from [Component.getConfigMap]. May be null.
         */
        fun pregenConfig(obj: Any, configMap: Map<String, IHaveConfig?>?) {
            val ms = obj.javaClass.declaredMethods
            for (m in ms) {
                if (m.returnType.name != ConfigData::class.java.name) continue
                val mName: String
                run {
                    val name = m.name
                    val ind = name.lastIndexOf('$')
                    mName = if (ind == -1) name else name.substring(ind + 1)
                }
                try {
                    m.isAccessible = true
                    var configList: List<ConfigData<*>>
                    configList = if (m.parameterCount == 0) {
                        listOf(m.invoke(obj) as ConfigData<*>)
                    } else if (m.parameterCount == 1 && m.parameterTypes[0] == IHaveConfig::class.java) {
                        if (configMap == null) continue  //Hope it will get called with the param later
                        configMap.entries.stream().map { (key, value): Map.Entry<String, IHaveConfig?> ->
                            try {
                                return@map m.invoke(obj, value) as ConfigData<*>
                            } catch (e: IllegalAccessException) {
                                val msg = "Failed to pregenerate $mName for $obj using config $key!"
                                if (obj is Component<*>) TBMCCoreAPI.SendException(
                                    msg,
                                    e,
                                    obj
                                ) else if (obj is JavaPlugin) TBMCCoreAPI.SendException(
                                    msg,
                                    e,
                                    obj
                                ) else TBMCCoreAPI.SendException(msg, e, false) { msg: String? ->
                                    Bukkit.getLogger().warning(msg)
                                }
                                return@map null
                            } catch (e: InvocationTargetException) {
                                val msg = "Failed to pregenerate $mName for $obj using config $key!"
                                if (obj is Component<*>) TBMCCoreAPI.SendException(
                                    msg,
                                    e,
                                    obj
                                ) else if (obj is JavaPlugin) TBMCCoreAPI.SendException(
                                    msg,
                                    e,
                                    obj
                                ) else TBMCCoreAPI.SendException(msg, e, false) { msg: String? ->
                                    Bukkit.getLogger().warning(msg)
                                }
                                return@map null
                            }
                        }
                            .filter(Predicate<ConfigData<Any?>> { obj: ConfigData<Any?>? -> Objects.nonNull(obj) })
                            .collect(Collectors.toList())
                    } else {
                        if (TBMCCoreAPI.IsTestServer()) MainPlugin.Instance.logger.warning(
                            "Method " + mName + " returns a config but its parameters are unknown: " + Arrays.toString(
                                m.parameterTypes
                            )
                        )
                        continue
                    }
                    for (c in configList) {
                        if (c.path.length == 0) c.setPath(mName) else if (c.path != mName) MainPlugin.Instance.logger.warning(
                            "Config name does not match: " + c.path + " instead of " + mName
                        )
                        c.get() //Saves the default value if needed - also checks validity
                    }
                } catch (e: Exception) {
                    val msg = "Failed to pregenerate $mName for $obj!"
                    if (obj is Component<*>) TBMCCoreAPI.SendException(
                        msg,
                        e,
                        obj
                    ) else if (obj is JavaPlugin) TBMCCoreAPI.SendException(msg, e, obj) else TBMCCoreAPI.SendException(
                        msg,
                        e,
                        false
                    ) { msg: String? -> Bukkit.getLogger().warning(msg) }
                }
            }
        }
    }
}