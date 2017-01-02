package buttondevteam.lib;

import java.util.UUID;

public abstract class TBMCPlayerBase extends ChromaGamerBase {
	public abstract UUID getUUID();

	public abstract String getPluginName();

	@Override
	public String getFileName() {
		return getUUID().toString();
	}

	@Override
	public String getFolder() {
		return "minecraft";
	}
}
