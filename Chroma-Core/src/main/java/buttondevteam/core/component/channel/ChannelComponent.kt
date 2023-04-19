package buttondevteam.core.component.channel

import buttondevteam.lib.ChromaUtils
import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.chat.*
import buttondevteam.lib.chat.Command2.*
import buttondevteam.lib.player.ChromaGamerBase
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
            get() = channel.extraIdentifiers.get().toTypedArray()

        @Subcommand
        fun def(senderMC: Command2MCSender, @OptionalArg @TextArg message: String?) {
            val sender = senderMC.sender
            val user = ChromaGamerBase.getFromSender(sender)
            if (user == null) {
                sender.sendMessage("${ChatColor.RED}You can't use channels from this platform.")
                return
            }
            if (message == null) {
                val oldch = user.channel.get()
                if (oldch is ChatRoom) oldch.leaveRoom(sender)
                if (oldch == channel) user.channel.set(Channel.globalChat) else {
                    user.channel.set(channel)
                    if (channel is ChatRoom) channel.joinRoom(sender)
                }
                sender.sendMessage("${ChatColor.GOLD}You are now talking in: ${ChatColor.AQUA}" + user.channel.get().displayName.get())
            } else TBMCChatAPI.sendChatMessage(
                ChatMessage.builder(sender, user, message).fromCommand(true)
                    .permCheck(senderMC.permCheck).build(), channel
            )
        }
    }

    companion object {
        var roomJoinLeave: BroadcastTarget? = null
    }
}