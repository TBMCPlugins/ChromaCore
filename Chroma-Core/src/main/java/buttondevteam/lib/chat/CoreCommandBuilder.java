package buttondevteam.lib.chat;

import buttondevteam.lib.chat.commands.CommandArgument;
import buttondevteam.lib.chat.commands.SubcommandData;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import java.util.Map;
import java.util.function.Function;

public class CoreCommandBuilder<S, TC extends ICommand2<?>> extends LiteralArgumentBuilder<S> {
	private final SubcommandData.SubcommandDataBuilder<TC> dataBuilder;

	protected CoreCommandBuilder(String literal, Class<?> senderType, Map<String, CommandArgument> arguments, TC command) {
		super(literal);
		dataBuilder = SubcommandData.<TC>builder().senderType(senderType).arguments(arguments).command(command);
	}

	@Override
	protected CoreCommandBuilder<S, TC> getThis() {
		return this;
	}

	public static <S, TC extends ICommand2<?>> CoreCommandBuilder<S, TC> literal(String name, Class<?> senderType, Map<String, CommandArgument> arguments, TC command) {
		return new CoreCommandBuilder<>(name, senderType, arguments, command);
	}

	public static <S, TC extends ICommand2<?>> CoreCommandBuilder<S, TC> literalNoOp(String name) {
		return literal(name, Command2Sender.class, Map.of(), null);
	}

	/**
	 * Static help text added through annotations. May be overwritten with the getter.
	 *
	 * @param helpText Help text shown to the user
	 * @return This instance
	 */
	public CoreCommandBuilder<S, TC> helps(String[] helpText) {
		dataBuilder.staticHelpText(helpText);
		return this;
	}

	/**
	 * Custom help text that depends on the context. Overwrites the static one.
	 * The function receives the sender but its type is not guaranteed to match the one at the subcommand.
	 * It will either match or be a Command2Sender, however.
	 *
	 * @param getter The getter function receiving the sender and returning the help text
	 * @return This instance
	 */
	public CoreCommandBuilder<S, TC> helps(Function<Object, String[]> getter) {
		dataBuilder.helpTextGetter(getter);
		return this;
	}

	@Override
	public CoreCommandNode<S, TC> build() {
		var result = new CoreCommandNode<>(this.getLiteral(), this.getCommand(), this.getRequirement(),
			this.getRedirect(), this.getRedirectModifier(), this.isFork(),
			dataBuilder.build());

		for (CommandNode<S> node : this.getArguments()) {
			result.addChild(node);
		}

		return result;
	}
}
