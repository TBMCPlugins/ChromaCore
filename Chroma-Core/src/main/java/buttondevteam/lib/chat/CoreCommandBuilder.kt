package buttondevteam.lib.chat

import buttondevteam.lib.chat.commands.CommandArgument
import buttondevteam.lib.chat.commands.NoOpSubcommandData
import buttondevteam.lib.chat.commands.SubcommandData
import com.mojang.brigadier.builder.LiteralArgumentBuilder

class CoreCommandBuilder<S : Command2Sender, TC : ICommand2<*>, TSD : NoOpSubcommandData> private constructor(
    literal: String,
    val data: TSD
) : LiteralArgumentBuilder<S>(literal) {

    override fun getThis(): CoreCommandBuilder<S, TC, TSD> {
        return this
    }

    override fun build(): CoreCommandNode<S, TSD> {
        val result = CoreCommandNode<_, _>(
            literal,
            command,
            requirement,
            this.redirect,
            this.redirectModifier,
            this.isFork,
            data
        )
        for (node in arguments) {
            result.addChild(node)
        }
        return result
    }

    companion object {
        /**
         * Start building an executable command node.
         *
         * @param name The subcommand name as written by the user
         * @param senderType The expected command sender type based on the subcommand method
         * @param arguments A map of the command arguments with their names as keys
         * @param argumentsInOrder A list of the command arguments in the order they are expected
         * @param command The command object that has this subcommand
         * @param helpTextGetter Custom help text that can depend on the context. The function receives the sender as the command itself receives it.
         * @param hasPermission A function that determines whether the user has permission to run this subcommand
         * @param annotations All annotations implemented by the method that executes the command
         * @param fullPath The full command path of this subcommand.
         */
        fun <S : Command2Sender, TC : ICommand2<*>> literal(
            name: String,
            senderType: Class<*>,
            arguments: Map<String, CommandArgument>,
            argumentsInOrder: List<CommandArgument>,
            command: TC,
            helpTextGetter: (Any) -> Array<String>,
            hasPermission: (S, SubcommandData<TC, S>) -> Boolean,
            annotations: Array<Annotation>,
            fullPath: String
        ): CoreCommandBuilder<S, TC, SubcommandData<TC, S>> {
            return CoreCommandBuilder(
                name,
                SubcommandData(senderType, arguments, argumentsInOrder, command, helpTextGetter, hasPermission, annotations, fullPath)
            )
        }

        /**
         * Start building a no-op command node.
         *
         * @param name The subcommand name as written by the user
         * @param helpTextGetter Custom help text that can depend on the context. The function receives the sender as the command itself receives it.
         */
        fun <S : Command2Sender, TC : ICommand2<*>> literalNoOp(
            name: String,
            helpTextGetter: (Any) -> Array<String>,
        ): CoreCommandBuilder<S, TC, NoOpSubcommandData> {
            return CoreCommandBuilder(name, NoOpSubcommandData(helpTextGetter))
        }
    }
}