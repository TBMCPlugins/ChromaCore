package buttondevteam.lib.chat

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionProvider

class CoreArgumentBuilder<S : Command2Sender, T>(
    private val name: String,
    private val type: ArgumentType<T>,
    private val optional: Boolean
) : ArgumentBuilder<S, CoreArgumentBuilder<S, T>>() {
    private var suggestionsProvider: SuggestionProvider<S>? = null
    fun suggests(provider: SuggestionProvider<S>): CoreArgumentBuilder<S, T> {
        suggestionsProvider = provider
        return this
    }

    override fun getThis(): CoreArgumentBuilder<S, T> {
        return this
    }

    override fun build(): CoreArgumentCommandNode<S, T> {
        val result = CoreArgumentCommandNode(
            name,
            type,
            command,
            requirement,
            redirect,
            redirectModifier,
            isFork,
            suggestionsProvider,
            optional
        )
        for (node in arguments) {
            result.addChild(node)
        }
        return result
    }

    override fun then(argument: ArgumentBuilder<S, *>?): CoreArgumentBuilder<S, T> {
        return super.then(argument)
    }

    companion object {
        fun <S : Command2Sender, T> argument(name: String, type: ArgumentType<T>, optional: Boolean): CoreArgumentBuilder<S, T> {
            return CoreArgumentBuilder(name, type, optional)
        }
    }
}