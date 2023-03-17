package buttondevteam.lib.architecture.config

interface IConfigData<T> {
    fun reset()
    fun get(): T?
    fun set(value: T?)

    val path: String
}