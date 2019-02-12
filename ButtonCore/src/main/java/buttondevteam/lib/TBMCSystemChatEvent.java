package buttondevteam.lib;

import buttondevteam.core.component.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Make sure to only send the message to users who {@link #shouldSendTo(CommandSender)} returns true.
 * 
 * @author NorbiPeti
 *
 */
@Getter
public class TBMCSystemChatEvent extends TBMCChatEventBase {
	private final String[] exceptions;
	private final BroadcastTarget target;
	private boolean handled;

	public void setHandled() {
		handled = true;
	}

	public TBMCSystemChatEvent(Channel channel, String message, int score, String groupid, String[] exceptions, BroadcastTarget target) { // TODO: Rich message
		super(channel, message, score, groupid);
		this.exceptions = exceptions;
		this.target = target;
	}

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class BroadcastTarget {
		private final @Getter String name;
		private static final HashSet<BroadcastTarget> targets = new HashSet<>();
		public static final BroadcastTarget ALL = new BroadcastTarget("ALL");

		public static BroadcastTarget add(String name) {
			val bt = new BroadcastTarget(Objects.requireNonNull(name));
			targets.add(bt);
			return bt;
		}

		public static void remove(BroadcastTarget target) {
			targets.remove(target);
		}

		@Nullable
		public static BroadcastTarget get(String name) {
			return targets.stream().filter(bt -> bt.name.equals(name)).findAny().orElse(null);
		}

		public static Stream<BroadcastTarget> stream() {
			return targets.stream();
		}
	}
}
