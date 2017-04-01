package buttondevteam.core;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import buttondevteam.core.TestPlayerClass.TestEnum;
import buttondevteam.lib.player.TBMCPlayerBase;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PlayerDataTest extends TestCase {
	public PlayerDataTest() {
		super("Player data test");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(PlayerDataTest.class);
	}

	public void testConfig() {
		Bukkit.setServer(Mockito.mock(Server.class, new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				// System.out.println("Return type: " + invocation.getMethod().getReturnType());
				// System.out.println(String.class.isAssignableFrom(invocation.getMethod().getReturnType()));
				if (String.class.isAssignableFrom(invocation.getMethod().getReturnType()))
					return "";
				if (Logger.class.isAssignableFrom(invocation.getMethod().getReturnType()))
					return Logger.getAnonymousLogger();
				if (PluginManager.class.isAssignableFrom(invocation.getMethod().getReturnType()))
					return Mockito.mock(PluginManager.class);
				return null;
			}
		}));
		UUID uuid = new UUID(0L, 0L);
		try (TestPlayerClass p = TBMCPlayerBase.getPlayer(uuid, TestPlayerClass.class)) {
			p.PlayerName().set("Test");
			assertEquals("Test", p.PlayerName().get());
			p.testenum().set(TestEnum.A); // TODO: Fix enum saving
			assertEquals(TestEnum.A, p.testenum().get());
			// p.TestShort().set((short) 5);
			// assertEquals((short) 5, (short) (int) p.TestShort().get());
			p.TestBool().set(true);
			assertEquals(true, (boolean) p.TestBool().get());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try (TestPlayerClass p = TBMCPlayerBase.getPlayer(uuid, TestPlayerClass.class)) {
			assertEquals("Test", p.PlayerName().get());
			assertEquals(TestEnum.A, p.testenum().get());
			// assertEquals((short) 5, (short) p.TestShort().get());
			assertEquals(true, (boolean) p.TestBool().get());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
