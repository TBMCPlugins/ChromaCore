package buttondevteam.lib.chat.commandargs;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
public class BetterStringArgumentType implements ArgumentType<String> {
	private final int len;

	public static BetterStringArgumentType word() {
		return new BetterStringArgumentType(-1);
	}

	public static BetterStringArgumentType word(int maxlen) {
		return new BetterStringArgumentType(maxlen);
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		if (len < 1)
			return reader.readStringUntil(' ');

		final int start = reader.getCursor();
		if (reader.canRead(len + 1) && reader.peek(len) != ' ')
			throw new SimpleCommandExceptionType(new LiteralMessage("String too long")).createWithContext(reader);
		for (int i = 0; i < len; i++)
			reader.skip();
		return reader.getString().substring(start, reader.getCursor());
	}

	@Override
	public Collection<String> getExamples() {
		return StringArgumentType.StringType.SINGLE_WORD.getExamples();
	}
}
