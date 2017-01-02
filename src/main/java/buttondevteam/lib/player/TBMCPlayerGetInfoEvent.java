package buttondevteam.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import buttondevteam.lib.TBMCPlayer.InfoTarget;

/**
 * <p>
 * This event gets called when player information is requested. It can be used to give more per-plugin information about a player.
 * </p>
 * 
 * @author Norbi
 *
 */
public class TBMCPlayerGetInfoEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private TBMCPlayer player;
	private List<String> infolines;
	private TBMCPlayer.InfoTarget target;

	TBMCPlayerGetInfoEvent(TBMCPlayer player, TBMCPlayer.InfoTarget target) {
		this.player = player;
		infolines = new ArrayList<>();
		this.target = target;
	}

	/**
	 * Get the {@link TBMCPlayer} object
	 * 
	 * @return A player object
	 */
	public TBMCPlayer getPlayer() {
		return player;
	}

	/**
	 * Add a line to the information text. The line should be in the format of <i>key: value</i> .
	 * 
	 * @param infoline
	 *            The line to add
	 */
	public void addInfo(String infoline) {
		infolines.add(infoline);
	}

	/**
	 * Get the {@link InfoTarget} we want the information for. Use this to format the returned string.
	 * 
	 * @return The target of the information.
	 */
	public TBMCPlayer.InfoTarget getTarget() {
		return target;
	}

	String getResult() {
		return infolines.stream().collect(Collectors.joining("\n"));
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
