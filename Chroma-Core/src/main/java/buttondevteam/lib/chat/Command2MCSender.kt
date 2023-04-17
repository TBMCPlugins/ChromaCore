package buttondevteam.lib.chat;

import buttondevteam.core.component.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class Command2MCSender implements Command2Sender {
	private @Getter final CommandSender sender;
	private @Getter final Channel channel;
	private @Getter final CommandSender permCheck;

	@Override
	public void sendMessage(String message) {
		sender.sendMessage(message);
	}

	@Override
	public void sendMessage(String[] message) {
		sender.sendMessage(message);
	}

	@Override
	public String getName() {
		return sender.getName();
	}
}
