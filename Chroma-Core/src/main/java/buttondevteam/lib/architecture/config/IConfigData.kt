package buttondevteam.lib.architecture.config

import buttondevteam.lib.architecture.ListConfigData

interface IConfigData<T> {
    /**
     * Gets the value from the config using the getter specified for the config. If the config is not set, the default value is returned.
     */
    fun get(): T

    /**
     * Sets the value in the config using the setter specified for the config. If the config is read-only, this does nothing.
     */
    fun set(value: T)

    /**
     * Reload the config from the file.
     */
    fun reload()

    /**
     * The path to the config value.
     */
    val path: String
}

interface IListConfigData<T> : IConfigData<ListConfigData<T>.List>
