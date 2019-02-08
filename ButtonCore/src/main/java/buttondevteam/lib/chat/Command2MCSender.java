package buttondevteam.lib.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class Command2MCSender implements Command2Sender {
	private @Getter final CommandSender sender;

	@Override
	public void sendMessage(String message) {
		sender.sendMessage(message);
	}

	@Override
	public void sendMessage(String[] message) {
		sender.sendMessage(message);
	}
}
