package buttondevteam.lib.player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import buttondevteam.lib.player.ChromaGamerBase.InfoTarget;

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

	private ChromaGamerBase player;
	private List<String> infolines;
	private InfoTarget target;

	TBMCPlayerGetInfoEvent(ChromaGamerBase player, InfoTarget target) {
		this.player = player;
		infolines = new ArrayList<>();
		this.target = target;
	}

	/**
	 * Get the {@link TBMCPlayer} object
	 * 
	 * @return A player object
	 */
	public ChromaGamerBase getPlayer() {
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
	public InfoTarget getTarget() {
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
