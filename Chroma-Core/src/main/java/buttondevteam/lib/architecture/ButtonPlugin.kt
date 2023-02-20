package buttondevteam.lib.architecture

import buttondevteam.buttonproc.HasConfig
import buttondevteam.core.ComponentManager
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.Component.Companion.updateConfig
import buttondevteam.lib.chat.Command2MC
import buttondevteam.lib.chat.Command2MC.registerCommand
import buttondevteam.lib.chat.Command2MC.unregisterCommands
import buttondevteam.lib.chat.ICommand2MC
import lombok.AccessLevel
import lombok.Getter
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

@HasConfig(global = true)
abstract class ButtonPlugin : JavaPlugin() {
    @Getter(AccessLevel.PROTECTED)
    private val iConfig = IHaveConfig { saveConfig() }
    private var yaml: CommentedConfiguration? = null

    @Getter(AccessLevel.PROTECTED)
    private val data //TODO
        : IHaveConfig? = null

    /**
     * Used to unregister components in the right order - and to reload configs
     */
    @Getter
    private val componentStack = Stack<Component<*>>()
    protected abstract fun pluginEnable()

    /**
     * Called after the components are unregistered
     */
    protected abstract fun pluginDisable()

    /**
     * Called before the components are unregistered
     */
    protected fun pluginPreDisable() {}
    override fun onEnable() {
        if (!loadConfig()) {
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

    private fun loadConfig(): Boolean {
        val config = config ?: return false
        var section = config.getConfigurationSection("global")
        if (section == null) section = config.createSection("global")
        iConfig.reset(section)
        return true
    }

    override fun onDisable() {
        try {
            pluginPreDisable()
            ComponentManager.unregComponents(this)
            pluginDisable()
            if (ConfigData.saveNow(config)) logger.info("Saved configuration changes.")
            ButtonPlugin.getCommand2MC().unregisterCommands(this)
        } catch (e: Exception) {
            TBMCCoreAPI.SendException("Error while disabling plugin $name!", e, this)
        }
    }

    override fun reloadConfig() {
        tryReloadConfig()
    }

    fun tryReloadConfig(): Boolean {
        if (!justReload()) return false
        loadConfig()
        componentStack.forEach(Consumer { c: Component<*>? -> updateConfig(this, c!!) })
        return true
    }

    fun justReload(): Boolean {
        if (yaml != null && ConfigData.saveNow(config)) {
            logger.warning("Saved pending configuration changes to the file, didn't reload. Apply your changes again.")
            return false
        }
        val file = File(dataFolder, "config.yml")
        val yaml = CommentedConfiguration(file)
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
        for ((key, value) in yc.getValues(true)) if (value is String) yaml.addComment(key.replace(".generalDescriptionInsteadOfAConfig", ""),
            *Arrays.stream<String>(value.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map<String> { str: String -> "# " + str.trim { it <= ' ' } }.toArray<String> { _Dummy_.__Array__() })
        return true
    }

    override fun getConfig(): FileConfiguration {
        if (yaml == null) justReload()
        return if (yaml == null) YamlConfiguration() else yaml //Return a temporary instance
    }

    override fun saveConfig() {
        try {
            if (yaml != null) yaml!!.save()
        } catch (e: Exception) {
            TBMCCoreAPI.SendException("Failed to save config", e, this)
        }
    }

    /**
     * Registers command and sets its plugin.
     *
     * @param command The command to register
     */
    fun registerCommand(command: ICommand2MC) {
        command.registerToPlugin(this)
        ButtonPlugin.getCommand2MC().registerCommand(command)
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
    annotation class ConfigOpts(val disableConfigGen: Boolean = false)
    companion object {
        @Getter //Needs to be static as we don't know the plugin when a command is handled

        private val command2MC = Command2MC()
        fun configGenAllowed(obj: Any): Boolean {
            return !Optional.ofNullable(obj.javaClass.getAnnotation(ConfigOpts::class.java))
                .map(Function<ConfigOpts, Boolean> { obj: ConfigOpts -> obj.disableConfigGen() }).orElse(false)
        }
    }
}