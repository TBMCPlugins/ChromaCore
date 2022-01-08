package buttondevteam.lib.chat;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CoreArgumentBuilder<S, T> extends ArgumentBuilder<S, CoreArgumentBuilder<S, T>> {
	private final String name;
	private final ArgumentType<T> type;
	private final boolean optional;
	private SuggestionProvider<S> suggestionsProvider = null;
	private String[] helpText = null; // TODO: Don't need the help text for arguments

	public CoreArgumentBuilder<S, T> suggests(SuggestionProvider<S> provider) {
		this.suggestionsProvider = provider;
		return this;
	}

	public CoreArgumentBuilder<S, T> helps(String[] helpText) {
		this.helpText = helpText;
		return this;
	}

	@Override
	protected CoreArgumentBuilder<S, T> getThis() {
		return this;
	}

	@Override
	public CoreArgumentCommandNode<S, T> build() {
		return new CoreArgumentCommandNode<>(name, type, getCommand(), getRequirement(), getRedirect(), getRedirectModifier(), isFork(), suggestionsProvider, optional, helpText);
	}
}
