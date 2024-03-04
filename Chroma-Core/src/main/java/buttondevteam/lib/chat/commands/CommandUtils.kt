package buttondevteam.lib.chat.commands

import buttondevteam.lib.ChromaUtils
import buttondevteam.lib.chat.*
import buttondevteam.lib.test.TestCommandFailedException
import com.google.common.base.Defaults
import com.google.common.primitives.Primitives
import com.mojang.brigadier.builder.ArgumentBuilder
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
     * Sends the given message (with no additional formatting) to the given sender or throws a special exception during testing.
     */
    fun reportUserError(sender: Command2Sender, message: String) {
        if (ChromaUtils.isTest) throw TestCommandFailedException(message)
        else sender.sendMessage(message)
    }

    /**
     * Performs the given action on the given node and all of its nodes recursively and creates new nodes.
     */
    fun <S : Command2Sender> mapSubcommands(node: CommandNode<S>, action: (CommandNode<S>) -> ArgumentBuilder<S, *>): CommandNode<S> {
        val newNode = action(node)
        node.children.map { mapSubcommands(it, action) }.forEach(newNode::then)
        return newNode.build()
    }

    /**
     * Casts the node to whatever you say if it's a command node. Use responsibly. Returns null if an argument node.
     *
     * Command nodes are nodes that are subcommands. They may do something or just print their help text.
     * They are definitely not argument nodes.
     */
    @Suppress("UNCHECKED_CAST")
    fun <TP : Command2Sender, TSD : NoOpSubcommandData> CommandNode<TP>.coreCommand(): CoreCommandNode<TP, TSD>? {
        return if (this.isCommand()) this as CoreCommandNode<TP, TSD>
        else null
    }

    /**
     * Returns the node as a probably-not-executable core command node or return null if it's an argument node.
     *
     * This method will work for any node as opposed to [CommandUtils.coreCommand].
     */
    fun <TP : Command2Sender> CommandNode<TP>.coreCommandNoOp(): CoreCommandNode<TP, NoOpSubcommandData>? {
        return coreCommand()
    }

    /**
     * Returns the node as an executable core command node or returns null if it's a no-op node.
     *
     * Executable nodes are valid command nodes that do something other than printing help text.
     */
    fun <TP : Command2Sender, TC : ICommand2<*>> CommandNode<TP>.coreExecutable(): CoreExecutableNode<TP, TC>? {
        return if (isExecutable()) coreCommand() else null
    }

    /**
     * Returns the node as an argument node or returns null if it's not one.
     *
     * Argument nodes are children of executable command nodes.
     */
    fun <TP : Command2Sender> CommandNode<TP>.coreArgument(): CoreArgumentCommandNode<TP, *>? {
        return if (this is CoreArgumentCommandNode<*, *>) this as CoreArgumentCommandNode<TP, *> else null
    }

    /**
     * Returns whether the current node is an executable or help text command node.
     */
    fun <TP : Command2Sender> CommandNode<TP>.isCommand(): Boolean {
        return this is CoreCommandNode<*, *>
    }

    /**
     * Returns whether the current node is an executable command node.
     */
    fun <TP : Command2Sender> CommandNode<TP>.isExecutable(): Boolean {
        return coreCommand<TP, NoOpSubcommandData>()?.data is SubcommandData<*, *>
    }

    @Suppress("UNCHECKED_CAST")
    fun <TP : Command2Sender, TC : ICommand2<TP>> CommandNode<TP>.subcommandData(): SubcommandData<TC, TP>? {
        return coreArgument()?.commandData as SubcommandData<TC, TP>?
            ?: coreExecutable<TP, TC>()?.data
    }

    fun <TP : Command2Sender> CommandNode<TP>.subcommandDataNoOp(): NoOpSubcommandData? {
        return subcommandData() ?: coreCommandNoOp()?.data
    }

    fun Class<*>.isEasilyRepresentable(): Boolean {
        return isPrimitive || Number::class.java.isAssignableFrom(this)
            || String::class.java.isAssignableFrom(this)
    }

    fun Class<*>.getDefaultForEasilyRepresentable(): Any? {
        return if (isPrimitive) {
            Defaults.defaultValue(this)
        } else if (Number::class.java.isAssignableFrom(this)) {
            Defaults.defaultValue(Primitives.unwrap(this))
        } else null
    }
}