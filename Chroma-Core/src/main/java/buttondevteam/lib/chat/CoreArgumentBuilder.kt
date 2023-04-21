package buttondevteam.lib.chat

import buttondevteam.lib.chat.commands.SubcommandData
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionProvider

class CoreArgumentBuilder<S : Command2Sender, T>(
    private val name: String,
    private val type: ArgumentType<T>,
    private val optional: Boolean
) : ArgumentBuilder<S, CoreArgumentBuilder<S, T>>() {
    private var suggestionsProvider: SuggestionProvider<S>? = null
    internal lateinit var data: SubcommandData<*, S>
    fun suggests(provider: SuggestionProvider<S>): CoreArgumentBuilder<S, T> {
        suggestionsProvider = provider
        return this
    }

    override fun getThis(): CoreArgumentBuilder<S, T> {
        return this
    }

    override fun build(): CoreArgumentCommandNode<S, T> {
        return CoreArgumentCommandNode(
            name,
            type,
            command,
            requirement,
            redirect,
            redirectModifier,
            isFork,
            suggestionsProvider,
            optional,
            data
        )
    }

    companion object {
        fun <S : Command2Sender, T> argument(name: String, type: ArgumentType<T>, optional: Boolean): CoreArgumentBuilder<S, T> {
            return CoreArgumentBuilder(name, type, optional)
        }
    }
}