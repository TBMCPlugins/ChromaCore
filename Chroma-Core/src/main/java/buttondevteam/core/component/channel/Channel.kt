package buttondevteam.core.component.channel

import buttondevteam.core.ComponentManager.get
import buttondevteam.core.MainPlugin
import buttondevteam.lib.architecture.ConfigData
import buttondevteam.lib.architecture.IHaveConfig
import buttondevteam.lib.architecture.ListConfigData
import buttondevteam.lib.chat.Color
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream

/**
 * Represents a chat channel. May only be instantiated after the channel component is registered.
 */
open class Channel
/**
 * Creates a channel.
 *
 * @param filterAndErrorMSG Checks all senders against the criteria provided here and sends the message if the index matches the sender's - if no score at all, displays the error.<br></br>
 * May be null to send to everyone.
 */(
    /**
     * The name that should appear at the start of the message. **A chat color is expected at the beginning (§9).**
     */
    private val defDisplayName: String,
    /**
     * The default color of the messages sent in the channel
     */
    private val defColor: Color,
    /**
     * The channel identifier. It's the same as the command to be used for the channel *without / *. For example "mod".
     * It's also used for scoreboard objective names.
     */
    val identifier: String,
    /**
     * A function that determines who has permission to see the channel.
     * If the sender doesn't have access, they cannot send the message.
     * Only those with access can see the messages.
     * If null, everyone has access.
     */
    private val filterAndErrorMSG: Function<CommandSender, RecipientTestResult>?
) {
    private val config: IHaveConfig? = null // TODO: Use this

    @JvmField
    val isEnabled = component.config.getData("${this.identifier}.enabled", true)

    /**
     * Must start with a color code
     */
    @JvmField
    val displayName: ConfigData<String> =
        component.config.getData("${this.identifier}.displayName", this.defDisplayName)

    @JvmField
    val color: ConfigData<Color> = component.config.getData("${this.identifier}.color",
        this.defColor, { c -> Color.valueOf((c as String)) }, Color::toString
    )

    @JvmField
    val extraIdentifiers: ListConfigData<String> = component.config.getListData("${this.identifier}.IDs", listOf())

    val isGlobal: Boolean
        get() = filterAndErrorMSG == null

    /**
     * Note: Errors are sent to the sender automatically
     *
     * @param sender The user we're sending to
     * @param score  The (source) score to compare with the user's
     */
    fun shouldSendTo(sender: CommandSender, score: Int): Boolean {
        return score == getMCScore(sender) //If there's any error, the score won't be equal
    }

    /**
     * Note: Errors are sent to the sender automatically
     */
    fun getMCScore(sender: CommandSender): Int {
        return getRTR(sender).score //No need to check if there was an error
    }

    /**
     * Note: Errors are sent to the sender automatically<br></br>
     *
     *
     * Null means don't send
     */
    fun getGroupID(sender: CommandSender): String? {
        return getRTR(sender).groupID //No need to check if there was an error
    }

    fun getRTR(sender: CommandSender): RecipientTestResult {
        return filterAndErrorMSG?.apply(sender) ?: RecipientTestResult(SCORE_SEND_OK, GROUP_EVERYONE)
    }

    class RecipientTestResult {
        @JvmField
        val errormessage: String?

        @JvmField
        val score // Anything below 0 is "never send"
            : Int

        @JvmField
        val groupID: String?

        /**
         * Creates a result that indicates an **error**
         *
         * @param errormessage The error message to show the sender if they don't meet the criteria.
         */
        constructor(errormessage: String?) {
            this.errormessage = errormessage
            score = SCORE_SEND_NOPE
            groupID = null
        }

        /**
         * Creates a result that indicates a **success**
         *
         * @param score   The score that identifies the target group. **Must be non-negative.** For example, the index of the town or nation to send to.
         * @param groupID The ID of the target group.
         */
        constructor(score: Int, groupID: String?) {
            require(score >= 0) { "Score must be non-negative!" }
            this.score = score
            this.groupID = groupID
            errormessage = null
        }

        companion object {
            @JvmField
            val ALL = RecipientTestResult(SCORE_SEND_OK, GROUP_EVERYONE)
        }
    }

    companion object {
        /**
         * Specifies a score that means it's OK to send - but it does not define any groups, only send or not send. See [.GROUP_EVERYONE]
         */
        const val SCORE_SEND_OK = 0

        /**
         * Specifies a score that means the user doesn't have permission to see or send the message. Any negative value has the same effect.
         */
        const val SCORE_SEND_NOPE = -1

        /**
         * Send the message to everyone *who has access to the channel* - this does not necessarily mean all players
         */
        const val GROUP_EVERYONE = "everyone"
        private val component: ChannelComponent by lazy {
            get(ChannelComponent::class.java)
                ?: throw RuntimeException("Attempting to create a channel before the component is registered!")
        }
        private val channels: MutableList<Channel> = ArrayList()

        /**
         * Get a stream of the enabled channels
         *
         * @return Only the enabled channels
         */
        @JvmStatic
        fun getChannels(): Stream<Channel> {
            return channels.stream().filter { ch: Channel -> ch.isEnabled.get() }
        }

        @JvmStatic
        val channelList: List<Channel>
            /**
             * Return all channels whether they're enabled or not
             *
             * @return A list of all channels
             */
            get() = Collections.unmodifiableList(channels)

        /**
         * Convenience method for the function parameter of [.Channel]. It checks if the sender is OP or optionally has the specified group. The error message is
         * generated automatically.
         *
         * @param permgroup The group that can access the channel or **null** to only allow OPs.
         * @return If has access
         */
        fun inGroupFilter(permgroup: String?): Function<CommandSender, RecipientTestResult> {
            return noScoreResult(
                { s ->
                    s.isOp || s is Player && permgroup?.let { pg ->
                        MainPlugin.permission?.playerInGroup(
                            s,
                            pg
                        )
                    } ?: false
                },
                "You need to be a(n) " + (permgroup ?: "OP") + " to use this channel."
            )
        }

        fun noScoreResult(
            filter: Predicate<CommandSender>,
            errormsg: String?
        ): Function<CommandSender, RecipientTestResult> {
            return Function { s ->
                if (filter.test(s)) RecipientTestResult(SCORE_SEND_OK, GROUP_EVERYONE)
                else RecipientTestResult(errormsg)
            }
        }

        @JvmField
        var GlobalChat: Channel? = null
        var AdminChat: Channel? = null
        var ModChat: Channel? = null

        @JvmStatic
        fun registerChannel(channel: Channel) {
            if (!channel.isGlobal && !component.isEnabled) return  //Allow registering the global chat (and I guess other chats like the RP chat)
            channels.add(channel)
            component.registerChannelCommand(channel)
            Bukkit.getScheduler().runTask(component.plugin,
                Runnable {
                    Bukkit.getPluginManager().callEvent(ChatChannelRegisterEvent(channel))
                }) // Wait for server start
        }
    }

}