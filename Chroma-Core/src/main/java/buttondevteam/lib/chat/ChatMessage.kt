package buttondevteam.lib.chat

import buttondevteam.lib.player.ChromaGamerBase
import org.bukkit.command.CommandSender
import java.util.*

class ChatMessage internal constructor(
    /**
     * The sender which sends the message.
     */
    val sender: Command2Sender,
    /**
     * The Chroma user which sends the message.
     */
    val user: ChromaGamerBase,
    /**
     * The message to send as the user.
     */
    var message: String,
    /**
     * Indicates whether the message comes from running a command (like /tableflip). Implemented to be used from Discord.
     */
    val isFromCommand: Boolean,
    /**
     * The sender which we should check for permissions. Same as [.sender] by default.
     */
    val permCheck: CommandSender,
    /**
     * The origin of the message, "Minecraft" or "Discord" for example. May be displayed to the user.
     *
     * **This is the user class capitalized folder name by default.**
     */
    val origin: String
) {

    class ChatMessageBuilder internal constructor(
        private var sender: CommandSender,
        private var user: ChromaGamerBase,
        private var message: String,
        private var origin: String
    ) {
        private var fromCommand = false
        private var permCheck: CommandSender? = null

        fun fromCommand(fromCommand: Boolean): ChatMessageBuilder {
            this.fromCommand = fromCommand
            return this
        }

        fun permCheck(permCheck: CommandSender): ChatMessageBuilder {
            this.permCheck = permCheck
            return this
        }

        fun origin(origin: String): ChatMessageBuilder {
            this.origin = origin
            return this
        }

        fun build(): ChatMessage {
            return ChatMessage(sender, user, message, fromCommand, permCheck ?: sender, origin)
        }

        override fun toString(): String {
            return "ChatMessage.ChatMessageBuilder(sender=$sender, user=$user, message=$message, fromCommand=$fromCommand, permCheck=$permCheck, origin=$origin)"
        }
    }

    companion object {
        @JvmStatic
        fun builder(sender: CommandSender, user: ChromaGamerBase, message: String): ChatMessageBuilder {
            return ChatMessageBuilder(
                sender, user, message,
                user.folder.substring(0, 1).uppercase(Locale.getDefault()) + user.folder.substring(1)
            )
        }
    }
}