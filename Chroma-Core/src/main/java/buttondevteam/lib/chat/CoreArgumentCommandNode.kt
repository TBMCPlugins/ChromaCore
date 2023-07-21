package buttondevteam.lib.chat

import buttondevteam.lib.chat.commands.SubcommandData
import com.mojang.brigadier.Command
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import java.util.function.Predicate

class CoreArgumentCommandNode<S : Command2Sender, T>(
    name: String?, type: ArgumentType<T>?, command: Command<S>?, requirement: Predicate<S>?, redirect: CommandNode<S>?, modifier: RedirectModifier<S>?, forks: Boolean, customSuggestions: SuggestionProvider<S>?,
    val optional: Boolean
) :
    ArgumentCommandNode<S, T>(name, type, command, requirement, redirect, modifier, forks, customSuggestions) {
    lateinit var commandData: SubcommandData<*, S>
        internal set // TODO: This should propagate to other arguments

    override fun getUsageText(): String {
        return if (optional) "[$name]" else "<$name>"
    }

    override fun createBuilder(): RequiredArgumentBuilder<S, T> {
        return super.createBuilder()
    }
}