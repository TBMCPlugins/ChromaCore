package buttondevteam.lib.chat;

public enum Priority {
	Low(0), Normal(1), High(2);
    private final int val;

	Priority(int v) {
		val = v;
	}

	public int GetValue() {
		return val;
	}
}
