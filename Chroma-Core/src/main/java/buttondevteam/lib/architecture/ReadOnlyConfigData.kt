package buttondevteam.lib.architecture

import java.util.function.Function

class ReadOnlyConfigData<T> internal constructor(
    config: IHaveConfig?,
    path: String,
    def: T?,
    primitiveDef: Any?,
    getter: Function<Any?, T>,
    setter: Function<T, Any?>
) : ConfigData<T>(config, path, def, primitiveDef, getter, setter)