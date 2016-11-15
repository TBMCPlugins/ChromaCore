package buttondevteam.lib.chat;

import java.util.ArrayList;
import java.util.List;

public class Channel {
	public final String DisplayName;
	public final Color color;
	public final String Command;

	private static List<Channel> channels = new ArrayList<>();

	public Channel(String displayname, Color color, String command) {
		DisplayName = displayname;
		this.color = color;
		Command = command;
	}

	static {
		channels.add(GlobalChat = new Channel("§fg§f", Color.White, "g"));
		channels.add(TownChat = new Channel("§3TC§f", Color.DarkAqua, "tc"));
		channels.add(NationChat = new Channel("§6NC§f", Color.Gold, "nc"));
		channels.add(AdminChat = new Channel("§cADMIN§f", Color.Red, "a"));
		channels.add(ModChat = new Channel("§9MOD§f", Color.Blue, "mod"));
	}

	public static List<Channel> getChannels() {
		return channels;
	}

	public static Channel GlobalChat;
	public static Channel TownChat;
	public static Channel NationChat;
	public static Channel AdminChat;
	public static Channel ModChat;
}
