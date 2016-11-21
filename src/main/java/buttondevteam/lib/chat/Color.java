package buttondevteam.lib.chat;

public enum Color implements TellrawSerializableEnum {
	Black("black"), DarkBlue("dark_blue"), DarkGreen("dark_green"), DarkAqua("dark_aqua"), DarkRed(
			"dark_red"), DarkPurple("dark_purple"), Gold("gold"), Gray("gray"), DarkGray("dark_gray"), Blue(
					"blue"), Green("green"), Aqua("aqua"), Red(
							"red"), LightPurple("light_purple"), Yellow("yellow"), White("white"), RPC("rpc");

	private String name;

	Color(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}