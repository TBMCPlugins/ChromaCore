package buttondevteam.lib.architecture.config

import kotlin.reflect.KProperty

class ConfigDataDelegate<T>(val data: IConfigData<T>) : IConfigData<T> by data {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = data.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = data.set(value)

    companion object {
        fun <T> IConfigData<T>.delegate() = ConfigDataDelegate(this)
    }
}
