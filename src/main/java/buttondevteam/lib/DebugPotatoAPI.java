package buttondevteam.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DebugPotatoAPI {
	public static ItemStack CreateDebugPotato(List<String> message) {
		ItemStack potato = new ItemStack(Material.BAKED_POTATO);
		ItemMeta meta = potato.getItemMeta();
		meta.setDisplayName("Spicy Debug Potato");
		meta.setLore(message);
		potato.setItemMeta(meta);
		potato.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 10);
		return potato;
	}

	public static ItemStack CreateDebugPotato(String message) {
		return CreateDebugPotato(WordWrap(message));
	}

	public static void SendDebugPotato(Player player, List<String> message) {
		player.getInventory().addItem(CreateDebugPotato(message));
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH, 0, 0);
		return;
	}

	public static void SendDebugPotato(Player player, String[] message) {
		SendDebugPotato(player, Arrays.asList(message));
	}

	public static void SendDebugPotato(Player player, String message) {
		SendDebugPotato(player, WordWrap(message));
	}

	public static List<String> WordWrap(String message) {
		String[] splitString = message.split("\\s+");
		List<String> newMessage = new ArrayList<String>();
		String currentLine = "";
		int currentLineLength = 0;
		int wordlength;
		int maxLineLength = 40;
		for (String word : splitString) {
			wordlength = word.length();
			if (currentLineLength == 0 || (currentLineLength + wordlength) < maxLineLength) {
				currentLine += word + " ";
				currentLineLength += wordlength + 1;
			} else {
				newMessage.add(currentLine);
				currentLine = word + " ";
				currentLineLength = word.length();
			}
		}
		return newMessage;
	}
}
