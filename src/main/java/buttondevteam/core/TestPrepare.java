package buttondevteam.core;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;

public class TestPrepare {
	public static void PrepareServer() {
		Bukkit.setServer(Mockito.mock(Server.class, new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				// System.out.println("Return type: " + invocation.getMethod().getReturnType());
				// System.out.println(String.class.isAssignableFrom(invocation.getMethod().getReturnType()));
				if (returns(invocation, String.class))
					return "test";
				if (returns(invocation, Logger.class))
					return Logger.getAnonymousLogger();
				if (returns(invocation, PluginManager.class))
					return Mockito.mock(PluginManager.class);
				if (returns(invocation, Collection.class))
					return Collections.EMPTY_LIST;
				return null;
			}

			boolean returns(InvocationOnMock invocation, Class<?> cl) {
				return cl.isAssignableFrom(invocation.getMethod().getReturnType());
			}
		}));
		TBMCChatAPI.RegisterChatChannel(Channel.GlobalChat = new Channel("§fg§f", Color.White, "g", null));
	}
}
