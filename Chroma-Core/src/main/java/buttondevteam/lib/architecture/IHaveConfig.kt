package buttondevteam.lib.architecture

import buttondevteam.lib.architecture.config.ConfigDataDelegate
import buttondevteam.lib.architecture.config.IConfigData
import buttondevteam.lib.architecture.config.ListConfigDataDelegate
import buttondevteam.lib.architecture.config.delegate
import org.bukkit.configuration.ConfigurationSection
import java.util.function.Function

/**
 * A config system
 * May be used in testing.
 */
class IHaveConfig(
    /**
     * The way the underlying configuration gets saved to disk
     */
    saveAction: Runnable,
    config: ConfigurationSection?
) {
    /**
     * The way the underlying configuration gets saved to disk
     */
    var saveAction = saveAction
        private set

    /**
     * Returns the Bukkit ConfigurationSection. Use [.signalChange] after changing it.
     */
    var config = config
        private set
    private val datamap = HashMap<String, IConfigData<*>>()

    /**
     * You may use this method with any data type, but always provide getters and setters that convert to primitive types
     * if you're not using primitive types directly.
     * These primitive types are strings, numbers, characters, booleans and lists of these things.
     *
     * @param path     The path in config to use
     * @param def      The value to use by default
     * @param getter   A function that converts a primitive representation to the correct value
     * @param setter   A function that converts a value to a primitive representation
     * @param readOnly If true, changing the value will have no effect
     * @param <T>     The type of this variable (can be any class)
     * @return The data object that can be used to get or set the value
    </T> */
    @Suppress("UNCHECKED_CAST")
    @JvmOverloads
    fun <T> getData(
        path: String,
        def: T,
        getter: Function<Any, T>? = null,
        setter: Function<T, Any>? = null,
        readOnly: Boolean = false
    ): ConfigDataDelegate<T> {
        val safeSetter = setter ?: Function { it ?: throw RuntimeException("No setter specified for nullable config data $path!") }
        return getData(path, getter ?: Function { it as T }, safeSetter, safeSetter.apply(def), readOnly)
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
        getter: Function<Any, T>,
        setter: Function<T, Any>,
        primitiveDef: Any,
        readOnly: Boolean = false
    ): ConfigDataDelegate<T> {
        val data =
            datamap[path] ?: ConfigData(this, path, primitiveDef, getter, setter, readOnly).also { datamap[path] = it }
        return (data as ConfigData<T>).delegate()
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
    ): ListConfigDataDelegate<T> {
        var data = datamap[path]
        if (data == null) datamap[path] = ListConfigData(
            this,
            path,
            ArrayList(def),
            elementGetter ?: Function { it as T },
            elementSetter ?: Function { it },
            readOnly
        ).also { data = it }
        @Suppress("UNCHECKED_CAST")
        return (data as ListConfigData<T>).delegate()
    }

    /**
     * Schedules a save operation. Use after changing the ConfigurationSection directly.
     */
    fun signalChange() {
        ConfigData.signalChange(this)
    }

    fun reload(section: ConfigurationSection, saveAction: Runnable = this.saveAction) {
        config = section
        this.saveAction = saveAction
        datamap.forEach { it.value.reload() }
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