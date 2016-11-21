package buttondevteam.lib;

import org.bukkit.entity.Player;

/**@deprecated
 * Fully Replaced by DebugPotato Class - Construct a DebugPotato*/
public class DebugPotatoAPI {
	/**@deprecated Replaced by DebugPotato.send*/
	public static void SendDebugPotato(DebugPotato dp, Player player) {
		player.getInventory().addItem(dp.toItemStack());
		return;
	}
}
