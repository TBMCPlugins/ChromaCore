package buttondevteam.core;

import buttondevteam.core.TestPlayerClass.TestEnum;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.TBMCPlayerBase;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.UUID;

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

	public void testConfig() throws Exception {
		TestPrepare.PrepareServer();
		FileUtils.deleteDirectory(new File(ChromaGamerBase.TBMC_PLAYERS_DIR));
		UUID uuid = new UUID(0L, 0L);
		try (TestPlayerClass p = TBMCPlayerBase.getPlayer(uuid, TestPlayerClass.class)) {
			p.PlayerName().set("Test");
			assertEquals("Test", p.PlayerName().get());
			assertEquals(TestEnum.A, p.testenum().get());
			assertEquals((short) 0, (short) p.TestShort().get());
            assertFalse(p.TestBool().get());
			p.testenum().set(TestEnum.B);
			assertEquals(TestEnum.B, p.testenum().get());
			p.TestShort().set((short) 5);
			assertEquals((short) 5, (short) p.TestShort().get());
			p.TestBool().set(true);
            assertTrue(p.TestBool().get());
		} catch (Exception e) {
			throw e;
		}
		try (TestPlayerClass p = TBMCPlayerBase.getPlayer(uuid, TestPlayerClass.class)) {
			assertEquals("Test", p.PlayerName().get());
			assertEquals(TestEnum.B, p.testenum().get());
			assertEquals((short) 5, (short) p.TestShort().get());
            assertTrue(p.TestBool().get());
		} catch (Exception e) {
			throw e;
		}
	}
}
