package buttondevteam.core;

import buttondevteam.lib.player.EnumPlayerData;
import buttondevteam.lib.player.PlayerClass;
import buttondevteam.lib.player.PlayerData;
import buttondevteam.lib.player.TBMCPlayerBase;

@PlayerClass(pluginname = "TestPlugin")
public class TestPlayerClass extends TBMCPlayerBase {
	public EnumPlayerData<TestEnum> testenum() {
		return dataEnum(TestEnum.class);
	}

	public enum TestEnum {
		A, B
	}

	public PlayerData<Short> TestShort() {
		return data();
	}

	public PlayerData<Boolean> TestBool() {
		return data();
	}
}
