package buttondevteam.lib.chat;

import buttondevteam.lib.chat.commands.SubcommandData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.Getter;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CoreCommandNode<T extends Command2Sender, TC extends ICommand2<?>> extends LiteralCommandNode<T> {
	@Getter
	private final SubcommandData<TC, T> data;

	public CoreCommandNode(String literal, Command<T> command, Predicate<T> requirement, CommandNode<T> redirect, RedirectModifier<T> modifier, boolean forks, SubcommandData<TC, T> data) {
		super(literal, command, requirement, redirect, modifier, forks);
		this.data = data;
	}

	/**
	 * @see #getChildren()
	 */
	public Collection<CoreCommandNode<T, TC>> getCoreChildren() {
		return super.getChildren().stream().map(node -> (CoreCommandNode<T, TC>) node).collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * @see #getChild(String)
	 */
	public CoreCommandNode<T, TC> getCoreChild(String name) {
		return (CoreCommandNode<T, TC>) super.getChild(name);
	}
}
