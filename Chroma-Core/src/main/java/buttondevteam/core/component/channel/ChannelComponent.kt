package buttondevteam.core.component.channel

import buttondevteam.core.MainPlugin
import buttondevteam.lib.ChromaUtils
import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.chat.*
import buttondevteam.lib.chat.Command2.*
import buttondevteam.lib.player.ChromaGamerBase
import org.bukkit.plugin.java.JavaPlugin

/**
 * Manages chat channels. If disabled, only global channels will be registered.
 */
class ChannelComponent : Component<MainPlugin>() {
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
        if (!ChromaUtils.isTest()) registerCommand(ChannelCommand(channel))
    }

    @CommandClass
    private class ChannelCommand(private val channel: Channel) : ICommand2MC() {
        override fun getCommandPath(): String {
            return channel.identifier
        }

        override fun getCommandPaths(): Array<String> {
            return channel.extraIdentifiers.get().toTypedArray()
        }

        @Subcommand
        fun def(senderMC: Command2MCSender, @OptionalArg @TextArg message: String?) {
            val sender = senderMC.sender
            val user = ChromaGamerBase.getFromSender(sender)
            if (user == null) {
                sender.sendMessage("§cYou can't use channels from this platform.")
                return
            }
            if (message == null) {
                val oldch = user.channel.get()
                if (oldch is ChatRoom) oldch.leaveRoom(sender)
                if (oldch == channel) user.channel.set(Channel.GlobalChat) else {
                    user.channel.set(channel)
                    if (channel is ChatRoom) channel.joinRoom(sender)
                }
                sender.sendMessage("§6You are now talking in: §b" + user.channel.get().displayName.get())
            } else TBMCChatAPI.SendChatMessage(
                ChatMessage.builder(sender, user, message).fromCommand(true)
                    .permCheck(senderMC.permCheck).build(), channel
            )
        }
    }

    companion object {
        var roomJoinLeave: BroadcastTarget? = null
    }
}