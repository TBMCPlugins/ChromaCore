package buttondevteam.lib.architecture

import buttondevteam.buttonproc.HasConfig
import buttondevteam.core.ComponentManager
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.exceptions.UnregisteredComponentException
import buttondevteam.lib.chat.ICommand2MC
import lombok.Getter
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

/**
 * Configuration is based on class name
 */
@HasConfig(global = false) //Used for obtaining javadoc

abstract class Component<TP : JavaPlugin?> {
    @Getter
    private var enabled = false

    @Getter
    private var plugin: TP = null

    @Getter
    private val config = IHaveConfig(null)

    @Getter
    private val data //TODO
        : IHaveConfig? = null

    @JvmField
    val shouldBeEnabled = config.getData("enabled",
        Optional.ofNullable(javaClass.getAnnotation(ComponentMetadata::class.java)).map(Function<ComponentMetadata, Boolean> { obj: ComponentMetadata -> obj.enabledByDefault() }).orElse(true))

    fun log(message: String) {
        plugin!!.logger.info("[" + className + "] " + message)
    }

    fun logWarn(message: String) {
        plugin!!.logger.warning("[" + className + "] " + message)
    }

    /**
     * Registers the module, when called by the JavaPlugin class.
     * This gets fired when the plugin is enabled. Use [.enable] to register commands and such.
     *
     * @param plugin Plugin object
     */
    protected open fun register(plugin: JavaPlugin?) {}

    /**
     * Unregisters the module, when called by the JavaPlugin class.
     * This gets fired when the plugin is disabled.
     * Do any cleanups needed within this method.
     *
     * @param plugin Plugin object
     */
    protected open fun unregister(plugin: JavaPlugin?) {}

    /**
     * Enables the module, when called by the JavaPlugin class. Call
     * registerCommand() and registerListener() within this method.<br></br>
     * To access the plugin, use [.getPlugin].
     */
    protected abstract fun enable()

    /**
     * Disables the module, when called by the JavaPlugin class. Do
     * any cleanups needed within this method.
     * To access the plugin, use [.getPlugin].
     */
    protected abstract fun disable()

    /**
     * Registers a command to the component. Make sure to use [buttondevteam.lib.chat.CommandClass] and [buttondevteam.lib.chat.Command2.Subcommand].
     * You don't need to register the command in plugin.yml.
     *
     * @param command Custom coded command class
     */
    fun registerCommand(command: ICommand2MC) {
        if (plugin is ButtonPlugin) command.registerToPlugin(plugin as ButtonPlugin)
        command.registerToComponent(this)
        ButtonPlugin.getCommand2MC().registerCommand(command)
    }

    /**
     * Registers a Listener to this component
     *
     * @param listener The event listener to register
     * @return The provided listener
     */
    protected fun registerListener(listener: Listener): Listener {
        TBMCCoreAPI.RegisterEventsForExceptions(listener, plugin)
        return listener
    }

    /**
     * Returns a map of configs that are under the given key.
     *
     * @param key             The key to use
     * @param defaultProvider A mapping between config paths and config generators
     * @return A map containing configs
     */
    fun getConfigMap(key: String?, defaultProvider: Map<String, Consumer<IHaveConfig?>>): Map<String, IHaveConfig> {
        val c: ConfigurationSection = getConfig().getConfig()
        var cs = c.getConfigurationSection(key)
        if (cs == null) cs = c.createSection(key)
        val res = cs!!.getValues(false).entries.stream().filter { (_, value): Map.Entry<String?, Any?> -> value is ConfigurationSection }
            .collect(Collectors.toMap<Map.Entry<String?, Any?>, String, IHaveConfig>(Function<Map.Entry<String?, Any?>, String> { (key1, value) -> java.util.Map.Entry.key }, Function<Map.Entry<String?, Any?>, IHaveConfig> { (_, value): Map.Entry<String?, Any?> ->
                val conf = IHaveConfig { getPlugin().saveConfig() }
                conf.reset(value as ConfigurationSection?)
                conf
            }))
        if (res.size == 0) {
            for ((key1, value) in defaultProvider) {
                val conf = IHaveConfig { getPlugin().saveConfig() }
                conf.reset(cs.createSection(key1))
                value.accept(conf)
                res[key1] = conf
            }
        }
        return res
    }

    private val className: String
        private get() = javaClass.simpleName

    companion object {
        private val components = HashMap<Class<out Component<*>>, Component<out JavaPlugin>>()

        /**
         * Registers a component checking it's dependencies and calling [.register].<br></br>
         * Make sure to register the dependencies first.<br></br>
         * The component will be enabled automatically, regardless of when it was registered.<br></br>
         * **If not using [ButtonPlugin], call [ComponentManager.unregComponents] on plugin disable.**
         *
         * @param component The component to register
         * @return Whether the component is registered successfully (it may have failed to enable)
         */
        @JvmStatic
        fun <T : JavaPlugin?> registerComponent(plugin: T, component: Component<T>): Boolean {
            return registerUnregisterComponent(plugin, component, true)
        }

        /**
         * Unregisters a component by calling [.unregister].<br></br>
         * Make sure to unregister the dependencies last.<br></br>
         * **Components will be unregistered in opposite order of registering by default by [ButtonPlugin] or [ComponentManager.unregComponents].**
         *
         * @param component The component to unregister
         * @return Whether the component is unregistered successfully (it also got disabled)
         */
        @JvmStatic
        fun <T : JavaPlugin?> unregisterComponent(plugin: T, component: Component<T>): Boolean {
            return registerUnregisterComponent(plugin, component, false)
        }

        fun <T : JavaPlugin?> registerUnregisterComponent(plugin: T, component: Component<T>, register: Boolean): Boolean {
            return try {
                val metaAnn = component.javaClass.getAnnotation(ComponentMetadata::class.java)
                if (metaAnn != null) {
                    val dependencies: Array<Class<out Component<*>>> = metaAnn.depends()
                    for (dep in dependencies) { //TODO: Support dependencies at enable/disable as well
                        if (!components.containsKey(dep)) {
                            plugin!!.logger.warning("Failed to " + (if (register) "" else "un") + "register component " + component.className + " as a required dependency is missing/disabled: " + dep.simpleName)
                            return false
                        }
                    }
                }
                if (register) {
                    if (components.containsKey(component.javaClass)) {
                        TBMCCoreAPI.SendException("Failed to register component " + component.className, IllegalArgumentException("The component is already registered!"), plugin)
                        return false
                    }
                    component.plugin = plugin
                    component.config.saveAction = Runnable { plugin!!.saveConfig() }
                    updateConfig(plugin, component)
                    component.register(plugin)
                    components[component.javaClass] = component
                    if (plugin is ButtonPlugin) (plugin as ButtonPlugin).componentStack.push(component)
                    if (ComponentManager.areComponentsEnabled() && component.shouldBeEnabled.get()) {
                        return try { //Enable components registered after the previous ones getting enabled
                            setComponentEnabled(component, true)
                            true
                        } catch (e: Exception) {
                            TBMCCoreAPI.SendException("Failed to enable component " + component.className + "!", e, component)
                            true
                        } catch (e: NoClassDefFoundError) {
                            TBMCCoreAPI.SendException("Failed to enable component " + component.className + "!", e, component)
                            true
                        }
                    }
                } else {
                    if (!components.containsKey(component.javaClass)) return true //Already unregistered
                    if (component.enabled) {
                        try {
                            setComponentEnabled(component, false)
                        } catch (e: Exception) {
                            TBMCCoreAPI.SendException("Failed to disable component " + component.className + "!", e, component)
                            return false //If failed to disable, won't unregister either
                        } catch (e: NoClassDefFoundError) {
                            TBMCCoreAPI.SendException("Failed to disable component " + component.className + "!", e, component)
                            return false
                        }
                    }
                    component.unregister(plugin)
                    components.remove(component.javaClass)
                }
                true
            } catch (e: Exception) {
                TBMCCoreAPI.SendException("Failed to " + (if (register) "" else "un") + "register component " + component.className + "!", e, plugin)
                false
            }
        }

        /**
         * Enables or disables the given component. If the component fails to enable, it will be disabled.
         *
         * @param component The component to register
         * @param enabled   Whether it's enabled or not
         */
        @JvmStatic
        @Throws(UnregisteredComponentException::class)
        fun setComponentEnabled(component: Component<*>, enabled: Boolean) {
            if (!components.containsKey(component.javaClass)) throw UnregisteredComponentException(component)
            if (component.enabled == enabled) return  //Don't do anything
            if (enabled.also { component.enabled = it }) {
                try {
                    updateConfig(component.getPlugin(), component)
                    component.enable()
                    if (ButtonPlugin.configGenAllowed(component)) {
                        IHaveConfig.pregenConfig(component, null)
                    }
                } catch (e: Exception) {
                    try { //Automatically disable components that fail to enable properly
                        setComponentEnabled(component, false)
                        throw e
                    } catch (ex: Exception) {
                        var t: Throwable = ex
                        var th: Throwable? = t
                        while (th != null) {
                            t = th //Set if not null
                            th = th.cause
                        }
                        if (t !== e) t.initCause(e)
                        throw ex
                    }
                }
            } else {
                component.disable()
                ButtonPlugin.getCommand2MC().unregisterCommands(component)
            }
        }

        @JvmStatic
        fun updateConfig(plugin: JavaPlugin, component: Component<*>) {
            if (plugin.config != null) { //Production
                var compconf = plugin.config.getConfigurationSection("components")
                if (compconf == null) compconf = plugin.config.createSection("components")
                var configSect = compconf!!.getConfigurationSection(component.className)
                if (configSect == null) configSect = compconf.createSection(component.className)
                component.config.reset(configSect)
            } //Testing: it's already set
        }

        /**
         * Returns the currently registered components<br></br>
         *
         * @return The currently registered components
         */
        @JvmStatic
        fun getComponents(): Map<Class<out Component<*>>, Component<out JavaPlugin>> {
            return Collections.unmodifiableMap(components)
        }
    }
}