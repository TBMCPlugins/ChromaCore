package buttondevteam.lib;

import org.bukkit.entity.Player;

public class DebugPotatoAPI {
	public static void SendDebugPotato(DebugPotato dp, Player player) {
		player.getInventory().addItem(dp.toItemStack());
		return;
	}
}
