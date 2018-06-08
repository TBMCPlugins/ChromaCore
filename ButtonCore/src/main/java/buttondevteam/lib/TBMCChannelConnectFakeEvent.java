package buttondevteam.lib;

import buttondevteam.lib.chat.Channel;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * The purpose of this event is to determine which group the given channel belongs to
 * or to validate that they have access to the given group chat.<br>
 * It's not meant to be called from any plugin - it should be only created to use the helper methods
 */
@Getter
public class TBMCChannelConnectFakeEvent extends TBMCChatEventBase implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Nullable
    private final CommandSender sender;

    /**
     * Using this the group will be determined based on the sender.
     *
     * @param sender  The sender to get the group from
     * @param channel The channel to use
     */
    public TBMCChannelConnectFakeEvent(CommandSender sender, Channel channel) {
        super(channel, "Channel connecting message. One of the things users should never see in action.", -1, null);
        this.sender = sender;
    }

    /**
     * Using this the given group will be validated and used.
     *
     * @param groupid The group to use, for example the name of a town or nation
     * @param channel The channel to use
     */
    public TBMCChannelConnectFakeEvent(String groupid, Channel channel) {
        super(channel, "Channel connecting message. One of the things users should never see in action.", -1, groupid);
        this.sender = null;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
