package buttondevteam.lib.chat

import buttondevteam.lib.player.ChromaGamerBase
import java.util.*

class ChatMessage internal constructor(
    /**
     * The Chroma user who sent the message.
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
    val permCheck: ChromaGamerBase,
    /**
     * The origin of the message, "Minecraft" or "Discord" for example. May be displayed to the user.
     *
     * **This is the user class capitalized folder name by default.**
     */
    val origin: String
) {

    class ChatMessageBuilder internal constructor(
        private var user: ChromaGamerBase,
        private var message: String,
        private var origin: String
    ) {
        private var fromCommand = false
        private var permCheck: ChromaGamerBase? = null

        fun fromCommand(fromCommand: Boolean): ChatMessageBuilder {
            this.fromCommand = fromCommand
            return this
        }

        fun permCheck(permCheck: ChromaGamerBase): ChatMessageBuilder {
            this.permCheck = permCheck
            return this
        }

        fun origin(origin: String): ChatMessageBuilder {
            this.origin = origin
            return this
        }

        fun build(): ChatMessage {
            return ChatMessage(user, message, fromCommand, permCheck ?: user, origin)
        }

        override fun toString(): String {
            return "ChatMessage.ChatMessageBuilder(user=$user, message=$message, fromCommand=$fromCommand, permCheck=$permCheck, origin=$origin)"
        }
    }

    companion object {
        @JvmStatic
        fun builder(user: ChromaGamerBase, message: String): ChatMessageBuilder {
            return ChatMessageBuilder(
                user, message,
                user.folder.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            )
        }
    }
}