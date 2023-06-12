package buttondevteam.core

import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.Component.Companion.components
import buttondevteam.lib.architecture.Component.Companion.setComponentEnabled
import buttondevteam.lib.architecture.Component.Companion.unregisterComponent
import org.bukkit.plugin.java.JavaPlugin

object ComponentManager {
    private var componentsEnabled = false

    /**
     * This flag is used to enable components registered after the others were enabled.
     * @return Whether already registered components have been enabled
     */
    fun areComponentsEnabled(): Boolean {
        return componentsEnabled
    }

    /**
     * Enables components based on a configuration - any component registered afterwards will be also enabled
     */
    fun enableComponents() {
        components.values.stream().filter { c: Component<out JavaPlugin> -> c.shouldBeEnabled }
            .forEach { c ->
                try {
                    setComponentEnabled(c, true)
                } catch (e: Exception) {
                    TBMCCoreAPI.SendException("Failed to enable one of the components: " + c.javaClass.simpleName, e, c)
                } catch (e: NoClassDefFoundError) {
                    TBMCCoreAPI.SendException("Failed to enable one of the components: " + c.javaClass.simpleName, e, c)
                }
            }
        componentsEnabled = true
    }

    /**
     * Unregister all components of a plugin that are enabled - called on [ButtonPlugin] disable
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : ButtonPlugin> unregComponents(plugin: T) {
        while (!plugin.componentStack.empty()) //Unregister in reverse order
            unregisterComponent(plugin, plugin.componentStack.pop() as Component<T>) //Components are pushed on register
    }

    /**
     * Will also return false if the component is not registered.
     *
     * @param cl The component class
     * @return Whether the component is registered and enabled
     */
    @JvmStatic
    fun isEnabled(cl: Class<out Component<*>?>?): Boolean {
        val c = components[cl]
        return c != null && c.isEnabled
    }

    /**
     * Will also return null if the component is not registered.
     *
     * @param cl The component class
     * @return The component if it's registered and enabled
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : Component<*>> getIfEnabled(cl: Class<T>): T? {
        val c = components[cl]
        return if (c != null && c.isEnabled) c as T else null
    }

    /**
     * It will return null if the component is not registered. Use this method if you don't want to check if the component is enabled.
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : Component<*>> get(cl: Class<T>): T? {
        return components[cl] as T?
    }
}