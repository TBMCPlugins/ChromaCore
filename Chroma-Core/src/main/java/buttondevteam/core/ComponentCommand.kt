package buttondevteam.core

import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.Component.Companion.components
import buttondevteam.lib.architecture.Component.Companion.setComponentEnabled
import buttondevteam.lib.chat.Command2.OptionalArg
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.CustomTabCompleteMethod
import buttondevteam.lib.chat.ICommand2MC
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.stream.Stream

@CommandClass(modOnly = true, helpText = ["Component command", "Can be used to enable/disable/list components"])
class ComponentCommand : ICommand2MC() {
    init {
        manager.addParamConverter(
            Plugin::class.java, { arg -> Bukkit.getPluginManager().getPlugin(arg) },
            "Plugin not found!"
        ) { Bukkit.getPluginManager().plugins.map { obj -> obj.name } }
    }

    @Subcommand(helpText = ["Enable component", "Temporarily or permanently enables a component."])
    fun enable(sender: CommandSender, plugin: Plugin, component: String, @OptionalArg permanent: Boolean): Boolean {
        if (plugin is ButtonPlugin) {
            if (!plugin.justReload()) {
                sender.sendMessage("${ChatColor.RED}Couldn't reload config, check console.")
                return true
            }
        } else plugin.reloadConfig() //Reload config so the new config values are read - All changes are saved to disk on disable
        return enable_disable(sender, plugin, component, true, permanent)
    }

    @Subcommand(helpText = ["Disable component", "Temporarily or permanently disables a component."])
    fun disable(sender: CommandSender, plugin: Plugin, component: String, @OptionalArg permanent: Boolean): Boolean {
        return enable_disable(sender, plugin, component, false, permanent)
    }

    @Subcommand(helpText = ["List components", "Lists all of the registered Chroma components"])
    fun list(sender: CommandSender, @OptionalArg plugin: String?): Boolean {
        sender.sendMessage("${ChatColor.GOLD}List of components:")
        //If plugin is null, don't check for it
        components.values.stream().filter { c -> plugin == null || c.plugin.name.equals(plugin, ignoreCase = true) }
            .map { c -> "${c.plugin.name} - ${c.javaClass.simpleName} - ${if (c.isEnabled) "en" else "dis"}abled" }
            .forEach { message -> sender.sendMessage(message) }
        return true
    }

    @CustomTabCompleteMethod(param = "component", subcommand = ["enable", "disable"])
    fun componentTabcomplete(plugin: Plugin): Iterable<String> {
        return Iterable { getPluginComponents(plugin).map { c -> c.javaClass.simpleName }.iterator() }
    }

    @CustomTabCompleteMethod(param = "plugin", subcommand = ["list", "enable", "disable"], ignoreTypeCompletion = true)
    fun pluginTabcomplete(): Iterable<String> {
        return Iterable { components.values.stream().map { it.plugin }.distinct().map { it.name }.iterator() }
    }

    private fun enable_disable(
        sender: CommandSender,
        plugin: Plugin,
        component: String,
        enable: Boolean,
        permanent: Boolean
    ): Boolean {
        try {
            val oc = getComponentOrError(plugin, component, sender)
            if (!oc.isPresent) return true
            setComponentEnabled(oc.get(), enable)
            if (permanent) oc.get().shouldBeEnabled.set(enable)
            sender.sendMessage("${oc.get().javaClass.simpleName} ${if (enable) "en" else "dis"}abled ${if (permanent) "permanently" else "temporarily"}.")
        } catch (e: Exception) {
            TBMCCoreAPI.SendException(
                "Couldn't " + (if (enable) "en" else "dis") + "able component " + component + "!",
                e,
                plugin as JavaPlugin
            )
        }
        return true
    }

    private fun getPluginComponents(plugin: Plugin): Stream<Component<out JavaPlugin>> {
        return components.values.stream().filter { c -> plugin.name == c.plugin.name }
    }

    private fun getComponentOrError(plugin: Plugin, arg: String, sender: CommandSender): Optional<Component<*>> {
        // TODO: Extend param converter to support accessing previous params
        val oc = getPluginComponents(plugin).filter { it.javaClass.simpleName.equals(arg, ignoreCase = true) }.findAny()
        if (!oc.isPresent) sender.sendMessage("${ChatColor.RED}Component not found!")
        return oc
    }
}