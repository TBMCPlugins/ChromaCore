package buttondevteam.lib.chat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

public class CoreCommandBuilder<S> extends LiteralArgumentBuilder<S> {
	private String[] helpText;

	protected CoreCommandBuilder(String literal) {
		super(literal);
	}

	@Override
	protected CoreCommandBuilder<S> getThis() {
		return this;
	}

	public static <S> CoreCommandBuilder<S> literal(String name) {
		return new CoreCommandBuilder<>(name);
	}

	public CoreCommandBuilder<S> helps(String[] helpText) {
		this.helpText = helpText;
		return this;
	}

	@Override
	public CoreCommandNode<S> build() {
		var result = new CoreCommandNode<>(this.getLiteral(), this.getCommand(), this.getRequirement(), this.getRedirect(), this.getRedirectModifier(), this.isFork(), helpText);

		for (CommandNode<S> node : this.getArguments()) {
			result.addChild(node);
		}

		return result;
	}
}
