package buttondevteam.lib.architecture

import buttondevteam.buttonproc.HasConfig
import buttondevteam.core.ComponentManager
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.chat.Command2MC
import buttondevteam.lib.chat.ICommand2MC
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer

@HasConfig(global = true)
abstract class ButtonPlugin : JavaPlugin {
    protected val iConfig = IHaveConfig(::saveConfig, section)
    private var yaml: YamlConfiguration? = null

    /**
     * May change if the config is reloaded
     */
    private val section get() = config.getConfigurationSection("global") ?: config.createSection("global")

    protected val data //TODO
        : IHaveConfig? = null

    /**
     * Used to unregister components in the right order - and to reload configs
     */
    val componentStack = Stack<Component<*>>()

    // Support testing with the protected constructor (MockBukkit)
    constructor() : super()
    protected constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File) : super(loader, description, dataFolder, file)

    protected abstract fun pluginEnable()

    /**
     * Called after the components are unregistered
     */
    protected abstract fun pluginDisable()

    /**
     * Called before the components are unregistered
     */
    protected open fun pluginPreDisable() {}
    override fun onEnable() {
        if (!tryReloadConfig()) {
            logger.warning("Please fix the issues and restart the server to load the plugin.")
            return
        }
        try {
            pluginEnable()
        } catch (e: Exception) {
            TBMCCoreAPI.SendException("Error while enabling plugin $name!", e, this)
        }
        if (configGenAllowed(this)) //If it's not disabled (by default it's not)
            IHaveConfig.pregenConfig(this, null)
    }

    override fun onDisable() {
        try {
            pluginPreDisable()
            ComponentManager.unregComponents(this)
            pluginDisable()
            if (ConfigData.saveNow(config)) logger.info("Saved configuration changes.")
            command2MC.unregisterCommands(this)
        } catch (e: Exception) {
            TBMCCoreAPI.SendException("Error while disabling plugin $name!", e, this)
        }
    }

    override fun reloadConfig() {
        tryReloadConfig()
    }

    fun tryReloadConfig(): Boolean {
        if (!saveAndReloadYaml()) return false
        iConfig.reload(section)
        componentStack.forEach(Consumer { c -> c.updateConfig() })
        return true
    }

    /**
     * Returns whether the config was loaded successfully. Otherwise, an empty config is used.
     */
    private fun saveAndReloadYaml(): Boolean {
        if (isConfigLoaded && ConfigData.saveNow(config)) {
            logger.warning("Saved pending configuration changes to the file, didn't reload. Apply your changes again.")
            return false
        }
        val file = File(dataFolder, "config.yml")
        val yaml = YamlConfiguration()
        if (file.exists()) {
            try {
                yaml.load(file)
            } catch (e: IOException) {
                logger.warning("Failed to load config! Check for syntax errors.")
                e.printStackTrace()
                return false
            } catch (e: InvalidConfigurationException) {
                logger.warning("Failed to load config! Check for syntax errors.")
                e.printStackTrace()
                return false
            }
        }
        this.yaml = yaml
        val res = getTextResource("configHelp.yml") ?: return true
        val yc = YamlConfiguration.loadConfiguration(res)
        for ((key, value) in yc.getValues(true))
            if (value is String) yaml.setComments(
                key.replace(".generalDescriptionInsteadOfAConfig", ""),
                value.split("\n").map { str -> "# " + str.trim { it <= ' ' } }
            )
        return true
    }

    override fun getConfig(): FileConfiguration {
        if (!isConfigLoaded) saveAndReloadYaml()
        return yaml ?: YamlConfiguration() //Return a temporary instance
    }

    override fun saveConfig() {
        if (isConfigLoaded) super.saveConfig()
    }

    val isConfigLoaded get() = yaml != null

    /**
     * Registers command and sets its plugin.
     *
     * @param command The command to register
     */
    fun registerCommand(command: ICommand2MC) {
        command.registerToPlugin(this)
        command2MC.registerCommand(command)
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
    annotation class ConfigOpts(val disableConfigGen: Boolean = false)
    companion object {
        //Needs to be static as we don't know the plugin when a command is handled
        @JvmStatic
        val command2MC = Command2MC()
        fun configGenAllowed(obj: Any): Boolean {
            return !Optional.ofNullable(obj.javaClass.getAnnotation(ConfigOpts::class.java))
                .map { it.disableConfigGen }.orElse(false)
        }
    }
}