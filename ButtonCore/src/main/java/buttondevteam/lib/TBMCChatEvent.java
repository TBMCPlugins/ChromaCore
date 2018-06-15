package buttondevteam.lib;

import buttondevteam.lib.chat.Channel;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * Make sure to only send the message to users where {@link #shouldSendTo(CommandSender)} returns true.
 * 
 * @author NorbiPeti
 *
 */
@Getter
public class TBMCChatEvent extends TBMCChatEventBase {
	public TBMCChatEvent(CommandSender sender, Channel channel, String message, int score, boolean fromcmd, String groupid) {
		super(channel, message, score, groupid);
		this.sender = sender;
		this.fromcmd = fromcmd;
        this.ignoreSenderPermissions = false;
    }

    public TBMCChatEvent(CommandSender sender, Channel channel, String message, int score, boolean fromcmd, String groupid, boolean ignoreSenderPermissions) {
        super(channel, message, score, groupid);
        this.sender = sender;
        this.fromcmd = fromcmd;
        this.ignoreSenderPermissions = ignoreSenderPermissions;
    }

	private static final HandlerList handlers = new HandlerList();

	private CommandSender sender;
	private boolean fromcmd;
    private final boolean ignoreSenderPermissions;
	// TODO: Message object with data?

    /**
     * This will allow the sender of the message if {@link #isIgnoreSenderPermissions()} is true.
     */
    @Override
    public boolean shouldSendTo(CommandSender sender) {
        if (isIgnoreSenderPermissions() && sender.equals(this.sender))
            return true; //Allow sending the message no matter what
        return super.shouldSendTo(sender);
    }

    /**
     * This will allow the sender of the message if {@link #isIgnoreSenderPermissions()} is true.
     */
    @Override
    public int getMCScore(CommandSender sender) {
        if (isIgnoreSenderPermissions() && sender.equals(this.sender))
            return getScore(); //Send in the correct group no matter what
        return super.getMCScore(sender);
    }

    /**
     * This will allow the sender of the message if {@link #isIgnoreSenderPermissions()} is true.
     */
    @Nullable
    @Override
    public String getGroupID(CommandSender sender) {
        if (isIgnoreSenderPermissions() && sender.equals(this.sender))
            return getGroupID(); //Send in the correct group no matter what
        return super.getGroupID(sender);
    }

    @Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
