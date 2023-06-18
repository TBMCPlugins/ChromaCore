package buttondevteam.lib.architecture.config

import buttondevteam.lib.architecture.ListConfigData
import kotlin.reflect.KProperty

open class ConfigDataDelegate<T>(private val data: IConfigData<T>) : IConfigData<T> by data {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = data.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = data.set(value)
}

class ListConfigDataDelegate<T>(data: IConfigData<ListConfigData<T>.List>) : ConfigDataDelegate<ListConfigData<T>.List>(data), IListConfigData<T>

fun <T> IConfigData<T>.delegate() = ConfigDataDelegate(this)
fun <T> IListConfigData<T>.delegate() = ListConfigDataDelegate(this)
