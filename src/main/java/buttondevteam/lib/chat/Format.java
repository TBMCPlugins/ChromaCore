package buttondevteam.lib.chat;

public enum Format implements TellrawSerializableEnum {
	Bold("bold"), Underlined("underlined"), Italic("italic"), Strikethrough("strikethrough"), Obfuscated(
			"obfuscated");
	// TODO: Add format codes to /u c <mode>
	private String name;

	Format(String name) {
		this.name = name;
		this.flag = 1 << this.ordinal();
	}

	@Override
	public String getName() {
		return name;
	}

	private final int flag;

	public int getFlag() {
		return flag;
	}
}