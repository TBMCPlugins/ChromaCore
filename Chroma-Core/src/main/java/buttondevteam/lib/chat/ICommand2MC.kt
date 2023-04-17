package buttondevteam.lib.chat

import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.architecture.ButtonPlugin.Companion.command2MC
import buttondevteam.lib.architecture.Component

abstract class ICommand2MC : ICommand2<Command2MCSender>(command2MC) {
    private var _plugin: ButtonPlugin? = null
    var plugin: ButtonPlugin
        get() = _plugin ?: throw IllegalStateException("The command is not registered to a plugin!")
        private set(value) {
            if (_plugin != null) throw IllegalStateException("The command is already assigned to a plugin!")
            _plugin = value
        }
    private var _component: Component<*>? = null
    var component: Component<*>
        get() = _component ?: throw IllegalStateException("The command is not registered to a component!")
        private set(value) {
            if (_component != null) throw IllegalStateException("The command is already assigned to a component!")
            _component = value
        }

    /**
     * Called from [buttondevteam.lib.architecture.Component.registerCommand] and [ButtonPlugin.registerCommand]
     */
    fun registerToPlugin(plugin: ButtonPlugin) {
        this.plugin = plugin
    }

    /**
     * Called from [buttondevteam.lib.architecture.Component.registerCommand]
     */
    fun registerToComponent(component: Component<*>) {
        this.component = component
    }
}