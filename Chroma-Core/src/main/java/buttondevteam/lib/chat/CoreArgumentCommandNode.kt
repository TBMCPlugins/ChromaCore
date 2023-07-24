package buttondevteam.lib.chat

import buttondevteam.lib.chat.commands.CommandUtils.coreArgument
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
    private var _commandData: SubcommandData<*, S>? = null
    var commandData: SubcommandData<*, S>
        get() {
            return _commandData
                ?: throw UninitializedPropertyAccessException("Command data has not been initialized")
        }
        internal set(value) {
            _commandData = value
            children.forEach { it.coreArgument()?.commandData = value }
        }

    override fun getUsageText(): String {
        return if (optional) "[$name]" else "<$name>"
    }

    override fun createBuilder(): RequiredArgumentBuilder<S, T> {
        return super.createBuilder()
    }
}