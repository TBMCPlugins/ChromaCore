package buttondevteam.lib;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DebugPotatoAPI {
	private static ItemStack CreateDebugPotato(DebugPotato dp) {
		ItemStack potato = new ItemStack(Material.BAKED_POTATO);
		ItemMeta meta = potato.getItemMeta();
		meta.setDisplayName(dp.getType() == null ? "Spicy Debug Potato" : dp.getType());
		if (dp.getMessage() == null)
			dp.setMessage("nullMessage");
		meta.setLore(dp.getMessage());
		potato.setItemMeta(meta);
		potato.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 10);
		return potato;
	}

	public static void SendDebugPotato(DebugPotato dp, Player player) {
		player.getInventory().addItem(CreateDebugPotato(dp));
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH, 0, 0);
		return;
	}
}
