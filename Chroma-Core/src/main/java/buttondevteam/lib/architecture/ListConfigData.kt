package buttondevteam.lib.architecture

import buttondevteam.lib.architecture.config.ConfigList
import buttondevteam.lib.architecture.config.IConfigData
import buttondevteam.lib.architecture.config.IListConfigData
import java.util.function.Function

class ListConfigData<T> internal constructor(
    config: IHaveConfig,
    path: String,
    primitiveDef: ArrayList<*>,
    internal val elementGetter: Function<Any?, T>,
    internal val elementSetter: Function<T, Any?>,
    readOnly: Boolean
) : IConfigData<ConfigList<T>>, IListConfigData<T> {
    val listConfig: ConfigData<ConfigList<T>> =
        ConfigData(config, path, primitiveDef, { ConfigList((it as ArrayList<*>).toMutableList(), this) }, { it }, readOnly)

    override val path get() = listConfig.path
    override fun get() = listConfig.get()
    override fun set(value: ConfigList<T>) = listConfig.set(value)
    override fun reload() = listConfig.reload()

}
