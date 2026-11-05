package buttondevteam.core.component.randomtp

import buttondevteam.core.MainPlugin
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.ComponentMetadata

/**
 * Teleport player to random location within world border.
 * Every five players teleport to the same general area, and then a new general area is randomly selected for the next five players.
 * Author: github.com/iiegit
 */
@ComponentMetadata(enabledByDefault = false)
class RandomTPComponent : Component<MainPlugin>() {
    override fun enable() {
        val rtp = RandomTP()
        registerCommand(rtp)
        rtp.onEnable(this)
    }

    override fun disable() {
    }
}
