package buttondevteam.lib.architecture.exceptions

import buttondevteam.lib.architecture.Component

class UnregisteredComponentException(component: Component<*>) : Exception("The component '${component.javaClass.getSimpleName()}' isn't registered!")
