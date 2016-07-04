package buttondevteam.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * <p>
 * Base class for other plugins' player data.
 * </p>
 * <ol>
 * <li>Add extra data fields</li>
 * <li>Add the extra data to the load/save methods</li>
 * </ol>
 * 
 * @author Norbi
 *
 */
public abstract class TBMCPlayerBase {
	public abstract void OnPlayerAdd(TBMCPlayerAddEvent event);

	public abstract void OnPlayerLoad();

	public abstract void OnPlayerSave();

	public abstract <T extends TBMCPlayerBase> T GetPlayerAs(TBMCPlayer player);

	public <T extends TBMCPlayerBase> T GetPlayerAs(Player player) {
		return GetPlayerAs(TBMCPlayer.GetPlayer(player));
	}
}
