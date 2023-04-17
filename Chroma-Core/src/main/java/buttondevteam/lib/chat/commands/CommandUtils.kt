package buttondevteam.lib.chat.commands

import buttondevteam.lib.chat.Command2Sender
import buttondevteam.lib.chat.CoreCommandNode
import buttondevteam.lib.chat.CoreExecutableNode
import buttondevteam.lib.chat.ICommand2
import com.mojang.brigadier.tree.CommandNode
import java.util.*

object CommandUtils {
    /**
     * Returns the path of the given subcommand excluding the class' path. It will start with the given replace char.
     *
     * @param methodName  The method's name, method.getName()
     * @param replaceChar The character to use between subcommands
     * @return The command path starting with the replacement char.
     */
    fun getCommandPath(methodName: String, replaceChar: Char): String {
        return if (methodName == "def") "" else replaceChar.toString() + methodName.replace('_', replaceChar)
            .lowercase(Locale.getDefault())
    }

    /**
     * Casts the node to whatever you say. Use responsibly.
     */
    @Suppress("UNCHECKED_CAST")
    fun <TP : Command2Sender, TSD : NoOpSubcommandData> CommandNode<TP>.core(): CoreCommandNode<TP, TSD> {
        return this as CoreCommandNode<TP, TSD>
    }

    /**
     * Returns the node as an executable core command node or returns null if it's a no-op node.
     */
    fun <TP : Command2Sender, TC : ICommand2<*>> CommandNode<TP>.coreExecutable(): CoreExecutableNode<TP, TC>? {
        val ret = core<TP, NoOpSubcommandData>()
        return if (ret.data is SubcommandData<*, *>) ret.core() else null
    }
}