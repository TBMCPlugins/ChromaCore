package buttondevteam.buttonproc

import java.lang.annotation.Inherited

/**
 * Used to generate documentation for the config
 */
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class HasConfig(val global: Boolean)
