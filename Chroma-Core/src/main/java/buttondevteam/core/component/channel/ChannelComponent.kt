package buttondevteam.core.component.channel

import buttondevteam.lib.ChromaUtils
import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.chat.*
import buttondevteam.lib.chat.Command2.*
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

/**
 * Manages chat channels. If disabled, only global channels will be registered.
 */
class ChannelComponent : Component<JavaPlugin>() {
    override fun register(plugin: JavaPlugin) {
        super.register(plugin)
        roomJoinLeave = BroadcastTarget.add("roomJoinLeave") //Even if it's disabled, global channels continue to work
    }

    override fun unregister(plugin: JavaPlugin) {
        super.unregister(plugin)
        BroadcastTarget.remove(roomJoinLeave)
        roomJoinLeave = null
    }

    override fun enable() {}
    override fun disable() {}
    fun registerChannelCommand(channel: Channel) {
        if (!ChromaUtils.isTest) registerCommand(ChannelCommand(channel))
    }

    @CommandClass
    private class ChannelCommand(private val channel: Channel) : ICommand2MC() {
        override val commandPath: String
            get() = channel.identifier
        override val commandPaths: Array<String>
            get() = channel.extraIdentifiers.toTypedArray()

        @Subcommand
        fun def(senderMC: Command2MCSender, @OptionalArg @TextArg message: String?) {
            val user = senderMC.sender
            if (message == null) {
                val oldch = user.channel
                if (oldch is ChatRoom) oldch.leaveRoom(user)
                if (oldch == channel) user.channel = Channel.globalChat else {
                    user.channel = channel
                    if (channel is ChatRoom) channel.joinRoom(user)
                }
                user.sendMessage("${ChatColor.GOLD}You are now talking in: ${ChatColor.AQUA}" + user.channel.displayName)
            } else TBMCChatAPI.sendChatMessage(
                ChatMessage.builder(user, message).fromCommand(true)
                    .permCheck(senderMC.permCheck).build(), channel
            )
        }
    }

    companion object {
        var roomJoinLeave: BroadcastTarget? = null
    }
}