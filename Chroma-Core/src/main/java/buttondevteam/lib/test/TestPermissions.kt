package buttondevteam.lib.test

import net.milkbowl.vault.permission.Permission

class TestPermissions : Permission() {
    override fun getName(): String = "TestPermissions"

    override fun isEnabled(): Boolean = true

    override fun hasSuperPermsCompat(): Boolean = false

    @Deprecated("Deprecated in Java")
    override fun playerHas(world: String?, player: String?, permission: String?): Boolean = true

    @Deprecated("Deprecated in Java")
    override fun playerAdd(world: String?, player: String?, permission: String?): Boolean = true

    @Deprecated("Deprecated in Java")
    override fun playerRemove(world: String?, player: String?, permission: String?): Boolean = true

    override fun groupHas(world: String?, group: String?, permission: String?): Boolean = true

    override fun groupAdd(world: String?, group: String?, permission: String?): Boolean = true

    override fun groupRemove(world: String?, group: String?, permission: String?): Boolean = true

    @Deprecated("Deprecated in Java")
    override fun playerInGroup(world: String?, player: String?, group: String?): Boolean = true

    @Deprecated("Deprecated in Java")
    override fun playerAddGroup(world: String?, player: String?, group: String?): Boolean = true

    @Deprecated("Deprecated in Java")
    override fun playerRemoveGroup(world: String?, player: String?, group: String?): Boolean = true

    @Deprecated("Deprecated in Java")
    override fun getPlayerGroups(world: String?, player: String?): Array<String> = arrayOf()

    @Deprecated("Deprecated in Java")
    override fun getPrimaryGroup(world: String?, player: String?): String = "default"

    override fun getGroups(): Array<String> = arrayOf()

    override fun hasGroupSupport(): Boolean = true
}