package buttondevteam.lib.chat;

public interface Command2Sender { //We don't need the 'extras' of CommandSender on Discord
	void sendMessage(String message);

	void sendMessage(String[] message);
}
