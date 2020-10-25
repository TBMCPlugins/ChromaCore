package buttondevteam.lib.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Per user class
 *
 * @param <T> The user class type, may be abstract
 */
@Getter
@RequiredArgsConstructor
public class StaticUserData<T extends ChromaGamerBase> {
	private final HashMap<Class<? extends T>, Supplier<T>> constructors = new HashMap<>();
	/**
	 * Key: User ID
	 */
	private final HashMap<String, CommonUserData<?>> userDataMap = new HashMap<>();
	private final String folder;
}
