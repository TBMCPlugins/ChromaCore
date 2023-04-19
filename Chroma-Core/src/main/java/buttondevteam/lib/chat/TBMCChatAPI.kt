package buttondevteam.lib.chat

import buttondevteam.core.component.channel.Channel
import buttondevteam.core.component.channel.Channel.Companion.channelList
import buttondevteam.core.component.channel.Channel.Companion.registerChannel
import buttondevteam.core.component.channel.Channel.RecipientTestResult
import buttondevteam.lib.ChromaUtils.callEventAsync
import buttondevteam.lib.ChromaUtils.doItAsync
import buttondevteam.lib.TBMCChatEvent
import buttondevteam.lib.TBMCChatPreprocessEvent
import buttondevteam.lib.TBMCSystemChatEvent
import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.function.Supplier

object TBMCChatAPI {
    /**
     * Sends a chat message to Minecraft. Make sure that the channel is registered with [.RegisterChatChannel].<br></br>
     * This will also send the error message to the sender, if they can't send the message.
     *
     * @param cm      The message to send
     * @param channel The MC channel to send in
     * @return The event cancelled state
     */
    @JvmOverloads
    fun sendChatMessage(cm: ChatMessage, channel: Channel = cm.user.channel.get()): Boolean {
        if (!channelList.contains(channel)) throw RuntimeException(
            "Channel " + channel.displayName.get() + " not registered!"
        )
        if (!channel.isEnabled.get()) {
            cm.sender.sendMessage("${ChatColor.RED}The channel '${channel.displayName.get()}' is disabled!")
            return true //Cancel sending if channel is disabled
        }
        val task = Supplier {
            val rtr = getScoreOrSendError(channel, cm.permCheck)
            val score = rtr.score
            if (score == Channel.SCORE_SEND_NOPE || rtr.groupID == null) return@Supplier true
            val eventPre = TBMCChatPreprocessEvent(cm.sender, channel, cm.message)
            Bukkit.getPluginManager().callEvent(eventPre)
            if (eventPre.isCancelled) return@Supplier true
            cm.message = eventPre.message
            val event = TBMCChatEvent(channel, cm, rtr)
            Bukkit.getPluginManager().callEvent(event)
            event.isCancelled
        }
        return doItAsync(task, false) //Not cancelled if async
    }

    /**
     * Sends a regular message to Minecraft. Make sure that the channel is registered with [.RegisterChatChannel].
     *
     * @param channel    The channel to send to
     * @param rtr        The score&group to use to find the group - use [RecipientTestResult.ALL] if the channel doesn't have scores
     * @param message    The message to send
     * @param exceptions Platforms where this message shouldn't be sent (same as [ChatMessage.origin]
     * @return The event cancelled state
     */
    @JvmStatic
    fun SendSystemMessage(
        channel: Channel,
        rtr: RecipientTestResult,
        message: String,
        target: BroadcastTarget?,
        vararg exceptions: String
    ): Boolean {
        if (!channelList.contains(channel)) throw RuntimeException("Channel " + channel.displayName.get() + " not registered!")
        if (!channel.isEnabled.get()) return true //Cancel sending
        if (!exceptions.contains("Minecraft")) Bukkit.getConsoleSender()
            .sendMessage("[" + channel.displayName.get() + "] " + message)
        val event = TBMCSystemChatEvent(channel, message, rtr.score, rtr.groupID!!, exceptions, target!!)
        return callEventAsync(event)
    }

    private fun getScoreOrSendError(channel: Channel, sender: CommandSender): RecipientTestResult {
        val result = channel.getRTR(sender)
        if (result.errormessage != null) sender.sendMessage("${ChatColor.RED}" + result.errormessage)
        return result
    }

    /**
     * Register a chat channel. See [Channel] for details.
     *
     * @param channel A new [Channel] to register
     */
    @JvmStatic
    fun registerChatChannel(channel: Channel) {
        registerChannel(channel)
    }
}