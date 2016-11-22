package buttondevteam.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DebugPotato {
	private List<String> message;
	private String type;

	/**
	 * Send the debug potato to a player
	 * 
	 * @param player
	 *            The player
	 */
	public void Send(Player player){
		player.getInventory().addItem(this.toItemStack());
		return;
	}

	/**
	 * Get the message (lore of the potato).
	 * 
	 * @return The message
	 */
	public List<String> getMessage() {
		return message;
	}

	/**
	 * Sets the message (lore of the potato).
	 * 
	 * @param message
	 *            The message
	 * @return This potato
	 */
	public DebugPotato setMessage(List<String> message) {
		this.message = message;
		return this;
	}

	/**
	 * Sets the message (lore of the potato). It will be word wrapped automatically.
	 * 
	 * @param message
	 *            The message
	 * @return This potato
	 */
	public DebugPotato setMessage(String message) {
		this.message = WordWrap(message);
		return this;
	}

	/**
	 * Sets the message (lore of the potato).
	 * 
	 * @param message
	 *            The message
	 * @return This potato
	 */
	public DebugPotato setMessage(String[] message) {
		this.message = Arrays.asList(message);
		return this;
	}

	/**
	 * Gets the type (potato name).
	 * 
	 * @return The type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type (potato name).
	 * 
	 * @param type
	 *            The type
	 * @return This potato
	 */
	public DebugPotato setType(String type) {
		this.type = type;
		return this;
	}

	private static List<String> WordWrap(String message) {
		String[] splitString = message.split("\\s+");
		List<String> newMessage = new ArrayList<String>();
		String currentLine = "";
		int currentLineLength = 0;
		int wordlength;
		int maxLineLength = 40;
		if (message.length() <= maxLineLength){
			newMessage.add(message);
			return newMessage;
		}
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
	public ItemStack toItemStack() {
		ItemStack potato = new ItemStack(Material.BAKED_POTATO);
		ItemMeta meta = potato.getItemMeta();
		meta.setDisplayName(this.getType() == null ? "Null Flavoured Debug Potato" : this.getType());
		if (this.getMessage() == null){
			List<String> message = new ArrayList<String>();
			message.add("nullMessage");
			meta.setLore(message);
		}else{
			meta.setLore(this.getMessage());
		}
		potato.setItemMeta(meta);
		potato.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 10);
		return potato;
	}
}
