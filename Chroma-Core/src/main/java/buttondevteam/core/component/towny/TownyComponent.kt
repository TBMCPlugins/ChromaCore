package buttondevteam.core.component.towny

import buttondevteam.core.MainPlugin
import buttondevteam.lib.architecture.Component

/**
 * Provides a command to remove invalid Towny residents.
 */
class TownyComponent : Component<MainPlugin>() {
    override fun enable() {
        registerCommand(RemoveResidentsCommand())
    }

    override fun disable() {
    }
}
