package buttondevteam.core.component.towny;

import buttondevteam.core.ComponentManager;
import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.Component;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.Bukkit;

/**
 * Automatically renames Towny players if they changed their Minecraft name
 */
public class TownyComponent extends Component<MainPlugin> {
	@Override
	protected void enable() {
		registerCommand(new RemoveResidentsCommand());
	}

	@Override
	protected void disable() {
	}

	/**
	 * Only renames the resident if this component is enabled. Used to handle name changes.
	 *
	 * @param oldName The player's old name as known by us
	 * @param newName The player's new name
	 */
	public static void renameInTowny(String oldName, String newName) {
		if (!ComponentManager.isEnabled(TownyComponent.class))
			return;
		Bukkit.getLogger().info("Renaming" + oldName + " in Towny to " + newName);
		TownyUniverse tu = Towny.getPlugin(Towny.class).getTownyUniverse();
		Resident resident = tu.getResidentMap().get(oldName.toLowerCase()); //The map keys are lowercase
		if (resident == null) {
			Bukkit.getLogger().warning("Resident not found - couldn't rename in Towny.");
			TBMCCoreAPI.sendDebugMessage("Resident not found - couldn't rename in Towny.");
		} else if (tu.getDataSource().hasResident(newName)) {
			Bukkit.getLogger().warning("Target resident name is already in use.");
			TBMCCoreAPI.sendDebugMessage("Target resident name is already in use. (" + oldName + " -> " + newName + ")");
		} else
			try {
				tu.getDataSource().renamePlayer(resident, newName); //Fixed in Towny 0.91.1.2
				Bukkit.getLogger().info("Renaming done.");
			} catch (AlreadyRegisteredException e) {
				TBMCCoreAPI.SendException("Failed to rename resident, there's already one with this name.", e);
			} catch (NotRegisteredException e) {
				TBMCCoreAPI.SendException("Failed to rename resident, the resident isn't registered.", e);
			}
	}
}
