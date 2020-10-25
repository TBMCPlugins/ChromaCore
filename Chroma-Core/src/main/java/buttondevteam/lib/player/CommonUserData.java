package buttondevteam.lib.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;

/**
 * Per user, regardless of actual type
 *
 * @param <T> The user class, may be abstract
 */
@Getter
@RequiredArgsConstructor
public class CommonUserData<T extends ChromaGamerBase> {
	private final HashMap<Class<? extends T>, ? extends T> userCache = new HashMap<>();
	private final YamlConfiguration playerData;
}
