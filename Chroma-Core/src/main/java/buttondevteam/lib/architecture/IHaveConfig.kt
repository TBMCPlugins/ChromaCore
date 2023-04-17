package buttondevteam.lib.architecture

import buttondevteam.lib.architecture.config.IConfigData
import org.bukkit.configuration.ConfigurationSection
import java.util.*
import java.util.function.Function

/**
 * A config system
 * May be used in testing.
 */
class IHaveConfig(
    /**
     * The way the underlying configuration gets saved to disk
     */
    val saveAction: Runnable,
    /**
     * Returns the Bukkit ConfigurationSection. Use [.signalChange] after changing it.
     */
    val config: ConfigurationSection
) {
    private val datamap = HashMap<String, IConfigData<*>>()

    /**
     * You may use this method with any data type, but always provide getters and setters that convert to primitive types
     * if you're not using primitive types directly.
     * These primitive types are strings, numbers, characters, booleans and lists of these things.
     *
     * @param path    The path in config to use
     * @param def     The value to use by default
     * @param getter  A function that converts a primitive representation to the correct value
     * @param setter  A function that converts a value to a primitive representation
     * @param primDef Whether the default value is a primitive value that needs to be converted to the correct type using the getter
     * @param <T>    The type of this variable (can be any class)
     * @return The data object that can be used to get or set the value
    </T> */
    @Suppress("UNCHECKED_CAST")
    @JvmOverloads
    fun <T> getData(
        path: String,
        def: T,
        getter: Function<Any?, T>? = null,
        setter: Function<T, Any?>? = null,
        readOnly: Boolean = false
    ): ConfigData<T> {
        return getData(path, getter ?: Function { it as T }, setter ?: Function { it }, def, readOnly)
    }

    /**
     * You may use this method with any data type and provide a primitive default value.
     * These primitive types are strings, numbers, characters, booleans and lists of these things.
     *
     * @param path         The path in config to use
     * @param primitiveDef The **primitive** value to use by default
     * @param getter       A function that converts a primitive representation to the correct value
     * @param setter       A function that converts a value to a primitive representation
     * @param <T>          The type of this variable (can be any class)
     * @return The data object that can be used to get or set the value
    </T> */
    @Suppress("UNCHECKED_CAST")
    @JvmOverloads
    fun <T> getData(
        path: String,
        getter: Function<Any?, T>,
        setter: Function<T, Any?>,
        primitiveDef: Any?,
        readOnly: Boolean = false
    ): ConfigData<T> {
        val data =
            datamap[path] ?: ConfigData(this, path, primitiveDef, getter, setter, readOnly).also { datamap[path] = it }
        return data as ConfigData<T>
    }

    /**
     * This method overload should only be used with primitves or String.
     *
     * @param path The path in config to use
     * @param <T>  The type of this variable (only use primitives or String)
     * @return The data object that can be used to get or set the value
    </T> */
    @Suppress("UNCHECKED_CAST")
    @JvmOverloads
    fun <T> getListData(
        path: String,
        def: List<T>,
        elementGetter: Function<Any?, T>? = null,
        elementSetter: Function<T, Any?>? = null,
        readOnly: Boolean = false
    ): ListConfigData<T> {
        var data = datamap[path]
        if (data == null) datamap[path] = ListConfigData(
            this,
            path,
            def,
            elementGetter ?: Function { it as T },
            elementSetter ?: Function { it },
            readOnly
        ).also { data = it }
        @Suppress("UNCHECKED_CAST")
        return data as ListConfigData<T>
    }

    /**
     * Schedules a save operation. Use after changing the ConfigurationSection directly.
     */
    fun signalChange() {
        ConfigData.signalChange(this)
    }

    companion object {
        /**
         * Generates the config YAML.
         *
         * @param obj       The object which has config methods
         * @param configMap The result from [Component.getConfigMap]. May be null.
         */
        fun pregenConfig(obj: Any, configMap: Map<String, IHaveConfig?>?) {
            // TODO: The configs are generated by ConfigData on creation
        }
    }
}