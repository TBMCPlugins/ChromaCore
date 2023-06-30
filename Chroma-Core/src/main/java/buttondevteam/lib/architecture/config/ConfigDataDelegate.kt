package buttondevteam.lib.architecture.config

import kotlin.reflect.KProperty

open class ConfigDataDelegate<T>(private val data: IConfigData<T>) : IConfigData<T> by data {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = data.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = data.set(value)
}

class ListConfigDataDelegate<T>(data: IListConfigData<T>) : ConfigDataDelegate<ConfigList<T>>(data), IListConfigData<T>

fun <T> IConfigData<T>.delegate() = ConfigDataDelegate(this)
fun <T> IListConfigData<T>.delegate() = ListConfigDataDelegate(this)
