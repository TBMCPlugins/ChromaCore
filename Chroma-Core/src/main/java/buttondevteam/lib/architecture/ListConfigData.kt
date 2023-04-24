package buttondevteam.lib.architecture

import buttondevteam.lib.architecture.config.IConfigData
import java.util.ArrayList
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.UnaryOperator

class ListConfigData<T> internal constructor(
    config: IHaveConfig?,
    path: String,
    primitiveDef: ArrayList<*>,
    private val elementGetter: Function<Any?, T>,
    private val elementSetter: Function<T, Any?>,
    readOnly: Boolean
) : IConfigData<ListConfigData<T>.List> {
    val listConfig: ConfigData<List> =
        ConfigData(config, path, primitiveDef, { List((it as ArrayList<*>).toMutableList()) }, { it }, readOnly)

    override val path: String get() = listConfig.path

    override fun get(): List {
        return listConfig.get()
    }

    override fun set(value: List?) {
        listConfig.set(value)
    }

    inner class List(backingList: MutableList<Any?>) : MutableList<T> {
        private val primitiveList = backingList
        override val size: Int get() = primitiveList.size
        private fun update() {
            val config = listConfig.config
            if (config != null) {
                ConfigData.signalChange(config) //Update the config model and start save task if needed
            }
        }

        override fun set(index: Int, element: T): T {
            val ret = primitiveList.set(index, elementSetter.apply(element))
            update()
            return elementGetter.apply(ret)
        }

        override fun add(element: T): Boolean {
            val ret = primitiveList.add(elementSetter.apply(element))
            update()
            return ret
        }

        override fun add(index: Int, element: T) {
            primitiveList.add(index, elementSetter.apply(element))
            update()
        }

        override fun removeAt(index: Int): T {
            val ret = primitiveList.removeAt(index)
            update()
            return elementGetter.apply(ret)
        }

        override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> =
            List(primitiveList.subList(fromIndex, toIndex))

        override fun remove(element: T): Boolean {
            val ret = primitiveList.remove(elementSetter.apply(element))
            update()
            return ret
        }

        override fun addAll(elements: Collection<T>): Boolean {
            val ret = primitiveList.addAll(elements.map { elementSetter.apply(it) })
            update()
            return ret
        }

        override fun addAll(index: Int, elements: Collection<T>): Boolean {
            val ret = primitiveList.addAll(index, elements.map { elementSetter.apply(it) })
            update()
            return ret
        }

        override fun removeAll(elements: Collection<T>): Boolean {
            val ret = primitiveList.removeAll(elements.map { elementSetter.apply(it) })
            update()
            return ret
        }

        override fun retainAll(elements: Collection<T>): Boolean {
            val ret = primitiveList.retainAll(elements.map { elementSetter.apply(it) })
            update()
            return ret
        }

        override fun removeIf(filter: Predicate<in T>): Boolean {
            val ret = primitiveList.removeIf { filter.test(elementGetter.apply(it)) }
            update()
            return ret
        }

        override fun replaceAll(operator: UnaryOperator<T>) {
            primitiveList.replaceAll { elementSetter.apply(operator.apply(elementGetter.apply(it))) }
            update()
        }

        override fun sort(c: Comparator<in T>) {
            primitiveList.sortWith { o1, o2 -> c.compare(elementGetter.apply(o1), elementGetter.apply(o2)) }
            update()
        }

        override fun clear() {
            primitiveList.clear()
            update()
        }

        override fun get(index: Int): T = elementGetter.apply(primitiveList[index])
        override fun isEmpty(): Boolean = primitiveList.isEmpty()

        override fun lastIndexOf(element: T): Int = primitiveList.lastIndexOf(elementSetter.apply(element))
        override fun indexOf(element: T): Int = primitiveList.indexOf(elementSetter.apply(element))
        override fun containsAll(elements: Collection<T>): Boolean =
            primitiveList.containsAll(elements.map { elementSetter.apply(it) })

        override fun contains(element: T): Boolean = primitiveList.contains(elementSetter.apply(element))
        override fun iterator(): MutableIterator<T> {
            return object : MutableIterator<T> {
                private val iterator = primitiveList.iterator()
                override fun hasNext(): Boolean = iterator.hasNext()
                override fun next(): T = elementGetter.apply(iterator.next())
                override fun remove() {
                    iterator.remove()
                    update()
                }
            }
        }

        override fun listIterator(): MutableListIterator<T> {
            return listIterator(0)
        }

        override fun listIterator(index: Int): MutableListIterator<T> {
            return object : MutableListIterator<T> {
                private val iterator = primitiveList.listIterator(index)
                override fun hasNext(): Boolean = iterator.hasNext()
                override fun next(): T = elementGetter.apply(iterator.next())
                override fun remove() {
                    iterator.remove()
                    update()
                }

                override fun hasPrevious(): Boolean = iterator.hasPrevious()
                override fun nextIndex(): Int = iterator.nextIndex()
                override fun previous(): T = elementGetter.apply(iterator.previous())
                override fun previousIndex(): Int = iterator.previousIndex()
                override fun add(element: T) {
                    iterator.add(elementSetter.apply(element))
                    update()
                }

                override fun set(element: T) {
                    iterator.set(elementSetter.apply(element))
                    update()
                }
            }
        }
    }
}
