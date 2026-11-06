package buttondevteam.lib.potato

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

open class DebugPotato {
    /**
     * Get the message (lore of the potato).
     * 
     * @return The message
     */
    var message: MutableList<String>? = null
        private set

    /**
     * Gets the type (potato name).
     * 
     * @return The type
     */
    var type: String? = null
        private set

    /**
     * Send the debug potato to a player
     * 
     * @param player
     * The player
     */
    fun Send(player: Player) {
        player.inventory.addItem(this.toItemStack())
        return
    }

    /**
     * Sets the message (lore of the potato).
     * @param message The message you wish to set
     * @param forceWordWrap Word Wraps the whole String List, without preserving previous line breaks. Preserves line breaks if false
     * @return This potato
     */
    fun setMessage(message: List<String>, forceWordWrap: Boolean): DebugPotato {
        if (forceWordWrap) {
            this.message = WordWrap(message.toString())
        } else {
            val outputList: MutableList<String> = ArrayList()
            for (line in message) {
                outputList.addAll(WordWrap(line))
            }
            this.message = outputList
        }

        return this
    }

    /**
     * Sets the message (lore of the potato). It will be word wrapped automatically.
     * @return This potato
     */
    open fun setMessage(message: List<String>): DebugPotato {
        return setMessage(message, false)
    }

    /**
     * Sets the message (lore of the potato). It will be word wrapped automatically.
     * @return This potato
     */
    open fun setMessage(message: String): DebugPotato {
        return setMessage(listOf(message))
    }

    /**
     * Sets the message (lore of the potato). It will be word wrapped automatically.
     * @return This potato
     */
    open fun setMessage(message: Array<String>): DebugPotato {
        return setMessage(listOf(*message))
    }

    /**
     * Sets the type (potato name).
     * 
     * @param type
     * The type
     * @return This potato
     */
    open fun setType(type: String?): DebugPotato {
        this.type = type
        return this
    }

    fun toItemStack(): ItemStack {
        val potato = ItemStack(Material.BAKED_POTATO)
        val meta = potato.itemMeta
        meta.displayName(Component.text(this.type ?: "Null Flavoured Debug Potato"))
        meta.lore(this.message?.map { Component.text(it) } ?: listOf(Component.text("nullMessage")))
        potato.itemMeta = meta
        potato.addUnsafeEnchantment(Enchantment.FLAME, 10)
        return potato
    }

    companion object {
        private fun WordWrap(message: String): MutableList<String> {
            val splitString = message.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val newMessage: MutableList<String> = ArrayList()
            var currentLine = ""
            var currentLineLength = 0
            var wordlength: Int
            val maxLineLength = 40
            if (message.length <= maxLineLength) {
                newMessage.add(message)
                return newMessage
            }
            for (word in splitString) {
                wordlength = word.length
                if (currentLineLength == 0 || (currentLineLength + wordlength) < maxLineLength) {
                    currentLine += "$word "
                    currentLineLength += wordlength + 1
                } else {
                    newMessage.add(currentLine)
                    currentLine = "$word "
                    currentLineLength = word.length
                }
            }
            newMessage.add(currentLine)
            return newMessage
        }
    }
}
