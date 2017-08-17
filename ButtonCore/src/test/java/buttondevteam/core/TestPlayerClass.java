package buttondevteam.core;

import buttondevteam.lib.player.EnumPlayerData;
import buttondevteam.lib.player.PlayerClass;
import buttondevteam.lib.player.PlayerData;
import buttondevteam.lib.player.TBMCPlayerBase;

@PlayerClass(pluginname = "TestPlugin")
public class TestPlayerClass extends TBMCPlayerBase {
	public EnumPlayerData<TestEnum> testenum() {
		return dataEnum(TestEnum.class, TestEnum.A);
	}

	public enum TestEnum {
		A, B
	}

	public PlayerData<Short> TestShort() {
		return data((short) 0);
	}

	public PlayerData<Boolean> TestBool() {
		return data(false);
	}
}
