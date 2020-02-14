package buttondevteam.core;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.core.component.channel.ChannelComponent;
import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

public class TestPrepare {
	public static void PrepareServer() {
		Bukkit.setServer(Mockito.mock(Server.class, new Answer<Object>() {

			@Override
            public Object answer(InvocationOnMock invocation) {
				if (returns(invocation, String.class))
					return "test";
				if (returns(invocation, Logger.class))
					return Logger.getAnonymousLogger();
				if (returns(invocation, PluginManager.class))
					return Mockito.mock(PluginManager.class);
				if (returns(invocation, Collection.class))
					return Collections.EMPTY_LIST;
				if (returns(invocation, BukkitScheduler.class))
					return Mockito.mock(BukkitScheduler.class);
				return null;
			}

			boolean returns(InvocationOnMock invocation, Class<?> cl) {
				return cl.isAssignableFrom(invocation.getMethod().getReturnType());
			}
		}));
		//noinspection unchecked
		Component.registerComponent(Mockito.mock(ButtonPlugin.class), new ChannelComponent());
		TBMCChatAPI.RegisterChatChannel(Channel.GlobalChat = new Channel("§fg§f", Color.White, "g", null));
	}
}
