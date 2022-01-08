package buttondevteam.lib.chat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.function.Predicate;

public class CoreCommandNode<T> extends LiteralCommandNode<T> {
	public CoreCommandNode(String literal, Command<T> command, Predicate<T> requirement, CommandNode<T> redirect, RedirectModifier<T> modifier, boolean forks, String helpText) {
		super(literal, command, requirement, redirect, modifier, forks);
	}
}
