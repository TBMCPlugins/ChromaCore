package buttondevteam.lib.architecture

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin

/**
 * A wrapper for plugin components. This is used internally.
 */
class ComponentData<TP : JavaPlugin>(
    val plugin: TP,
    saveAction: Runnable,
    config: ConfigurationSection
) {
    val config = IHaveConfig(saveAction, config) // TODO: Use lateinit instead of this class
}