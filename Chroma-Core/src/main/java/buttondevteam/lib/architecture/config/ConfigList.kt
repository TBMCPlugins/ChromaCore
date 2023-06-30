package buttondevteam.lib.architecture.config

import buttondevteam.lib.architecture.ConfigData
import buttondevteam.lib.architecture.ListConfigData
import java.util.function.Predicate
import java.util.function.UnaryOperator

class ConfigList<T>(
    backingList: MutableList<Any?>,
    private val parentConfig: ListConfigData<T>
) : MutableList<T> {
    private val primitiveList = backingList
    override val size: Int get() = primitiveList.size
    private fun update() {
        val config = parentConfig.listConfig.config
        ConfigData.signalChange(config) //Update the config model and start save task if needed
    }

    override fun set(index: Int, element: T): T {
        val ret = primitiveList.set(index, parentConfig.elementSetter.apply(element))
        update()
        return parentConfig.elementGetter.apply(ret)
    }

    override fun add(element: T): Boolean {
        val ret = primitiveList.add(parentConfig.elementSetter.apply(element))
        update()
        return ret
    }

    override fun add(index: Int, element: T) {
        primitiveList.add(index, parentConfig.elementSetter.apply(element))
        update()
    }

    override fun removeAt(index: Int): T {
        val ret = primitiveList.removeAt(index)
        update()
        return parentConfig.elementGetter.apply(ret)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> =
        ConfigList(primitiveList.subList(fromIndex, toIndex), parentConfig)

    override fun remove(element: T): Boolean {
        val ret = primitiveList.remove(parentConfig.elementSetter.apply(element))
        update()
        return ret
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val ret = primitiveList.addAll(elements.map { parentConfig.elementSetter.apply(it) })
        update()
        return ret
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val ret = primitiveList.addAll(index, elements.map { parentConfig.elementSetter.apply(it) })
        update()
        return ret
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val ret = primitiveList.removeAll(elements.map { parentConfig.elementSetter.apply(it) })
        update()
        return ret
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val ret = primitiveList.retainAll(elements.map { parentConfig.elementSetter.apply(it) })
        update()
        return ret
    }

    override fun removeIf(filter: Predicate<in T>): Boolean {
        val ret = primitiveList.removeIf { filter.test(parentConfig.elementGetter.apply(it)) }
        update()
        return ret
    }

    override fun replaceAll(operator: UnaryOperator<T>) {
        primitiveList.replaceAll { parentConfig.elementSetter.apply(operator.apply(parentConfig.elementGetter.apply(it))) }
        update()
    }

    override fun sort(c: Comparator<in T>) {
        primitiveList.sortWith { o1, o2 -> c.compare(parentConfig.elementGetter.apply(o1), parentConfig.elementGetter.apply(o2)) }
        update()
    }

    override fun clear() {
        primitiveList.clear()
        update()
    }

    override fun get(index: Int): T = parentConfig.elementGetter.apply(primitiveList[index])
    override fun isEmpty(): Boolean = primitiveList.isEmpty()

    override fun lastIndexOf(element: T): Int = primitiveList.lastIndexOf(parentConfig.elementSetter.apply(element))
    override fun indexOf(element: T): Int = primitiveList.indexOf(parentConfig.elementSetter.apply(element))
    override fun containsAll(elements: Collection<T>): Boolean =
        primitiveList.containsAll(elements.map { parentConfig.elementSetter.apply(it) })

    override fun contains(element: T): Boolean = primitiveList.contains(parentConfig.elementSetter.apply(element))
    override fun iterator(): MutableIterator<T> {
        return object : MutableIterator<T> {
            private val iterator = primitiveList.iterator()
            override fun hasNext(): Boolean = iterator.hasNext()
            override fun next(): T = parentConfig.elementGetter.apply(iterator.next())
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
            override fun next(): T = parentConfig.elementGetter.apply(iterator.next())
            override fun remove() {
                iterator.remove()
                update()
            }

            override fun hasPrevious(): Boolean = iterator.hasPrevious()
            override fun nextIndex(): Int = iterator.nextIndex()
            override fun previous(): T = parentConfig.elementGetter.apply(iterator.previous())
            override fun previousIndex(): Int = iterator.previousIndex()
            override fun add(element: T) {
                iterator.add(parentConfig.elementSetter.apply(element))
                update()
            }

            override fun set(element: T) {
                iterator.set(parentConfig.elementSetter.apply(element))
                update()
            }
        }
    }
}