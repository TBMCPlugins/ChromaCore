package buttondevteam.lib.architecture

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComponentMetadata(val depends: Array<KClass<out Component<*>>> = [], val enabledByDefault: Boolean = true)
