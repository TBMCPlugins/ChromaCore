package buttondevteam.lib.chat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;

import java.util.function.Predicate;

public class CoreArgumentCommandNode<S, T> extends ArgumentCommandNode<S, T> {
	private final boolean optional;
	@lombok.Getter private final String[] helpText;

	public CoreArgumentCommandNode(String name, ArgumentType<T> type, Command<S> command, Predicate<S> requirement, CommandNode<S> redirect, RedirectModifier<S> modifier, boolean forks, SuggestionProvider<S> customSuggestions, boolean optional, String[] helpText) {
		super(name, type, command, requirement, redirect, modifier, forks, customSuggestions);
		this.optional = optional;
		this.helpText = helpText;
	}

	@Override
	public String getUsageText() {
		return optional ? "[" + getName() + "]" : "<" + getName() + ">";
	}

	@Override
	public RequiredArgumentBuilder<S, T> createBuilder() {
		return super.createBuilder();
	}
}
