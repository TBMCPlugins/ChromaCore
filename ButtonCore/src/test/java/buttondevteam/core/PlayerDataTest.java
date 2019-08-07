package buttondevteam.core;

import buttondevteam.core.TestPlayerClass.TestEnum;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.TBMCPlayerBase;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
		//FileUtils.deleteDirectory(new File(ChromaGamerBase.TBMC_PLAYERS_DIR));
		Files.walkFileTree(new File(ChromaGamerBase.TBMC_PLAYERS_DIR).toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e)
				throws IOException {
				if (e == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed
					throw e;
				}
			}
		});
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
