package buttondevteam.core

import be.seeseemelk.mockbukkit.MockBukkit
import buttondevteam.core.component.channel.Channel
import buttondevteam.core.component.channel.ChannelComponent
import buttondevteam.lib.ChromaUtils.isTest
import buttondevteam.lib.architecture.Component.Companion.registerComponent
import buttondevteam.lib.chat.Color
import buttondevteam.lib.chat.TBMCChatAPI.registerChatChannel
import org.bukkit.ChatColor

@Deprecated("Use MockBukkit")
object TestPrepare {
    @JvmStatic
    fun prepareServer() {
        isTest = true //Needs to be in a separate class because of the potential lack of Mockito
        MockBukkit.mock()
        registerComponent(MockBukkit.load(MainPlugin::class.java), ChannelComponent())
        registerChatChannel(Channel("${ChatColor.WHITE}g${ChatColor.WHITE}", Color.White, "g", null).also { Channel.globalChat = it })
    }
}
