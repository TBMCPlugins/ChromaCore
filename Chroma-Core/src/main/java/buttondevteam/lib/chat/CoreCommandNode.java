package buttondevteam.lib.chat;

import buttondevteam.lib.chat.commands.SubcommandData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.Getter;

import java.util.function.Predicate;

public class CoreCommandNode<T, TC extends ICommand2<?>> extends LiteralCommandNode<T> {
	@Getter
	private final SubcommandData<TC> data;

	public CoreCommandNode(String literal, Command<T> command, Predicate<T> requirement, CommandNode<T> redirect, RedirectModifier<T> modifier, boolean forks, SubcommandData<TC> data) {
		super(literal, command, requirement, redirect, modifier, forks);
		this.data = data;
	}
}
