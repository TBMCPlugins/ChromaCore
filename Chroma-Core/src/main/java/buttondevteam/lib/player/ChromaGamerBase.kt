package buttondevteam.lib.player

import buttondevteam.core.MainPlugin
import buttondevteam.core.component.channel.Channel
import buttondevteam.core.component.channel.Channel.Companion.getChannels
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.architecture.ConfigData
import buttondevteam.lib.architecture.ConfigData.Companion.saveNow
import buttondevteam.lib.architecture.IHaveConfig
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

@ChromaGamerEnforcer
abstract class ChromaGamerBase {
    lateinit var config: IHaveConfig
        protected set

    protected lateinit var commonUserData: CommonUserData<out ChromaGamerBase>
    protected open fun init() {
        config = IHaveConfig({ save() }, commonUserData.playerData)
    }

    protected fun updateUserConfig() {} // TODO: Use this instead of reset()

    /**
     * Saves the player. It'll handle all exceptions that may happen. Called automatically.
     */
    protected open fun save() {
        try {
            if (commonUserData.playerData.getKeys(false).size > 0)
                commonUserData.playerData.save(File(TBMC_PLAYERS_DIR + folder, "$fileName.yml"))
        } catch (e: Exception) {
            TBMCCoreAPI.SendException("Error while saving player to $folder/$fileName.yml!", e, MainPlugin.instance)
        }
    }

    /**
     * Removes the user from the cache. This will be called automatically after some time by default.
     */
    fun uncache() {
        val userCache = commonUserData.userCache
        synchronized(userCache) {
            if (userCache.containsKey(javaClass))
                check(userCache.remove(javaClass) === this) { "A different player instance was cached!" }
        }
    }

    protected open fun scheduleUncache() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(
            MainPlugin.instance,
            Runnable { uncache() },
            (2 * 60 * 60 * 20).toLong()
        ) //2 hours
    }

    /**
     * Connect two accounts. Do not use for connecting two Minecraft accounts or similar. Also make sure you have the "id" tag set.
     *
     * @param user The account to connect with
     */
    fun <T : ChromaGamerBase> connectWith(user: T) {
        synchronized(staticDataMap) {
            // Set the ID, go through all linked files and connect them as well
            if (folder.equals(user.folder, ignoreCase = true))
                throw RuntimeException("Do not connect two accounts of the same type! Type: $folder")
            // This method acts from the point of view of the source, the target is the other way around
            fun sync(us: ChromaGamerBase, them: ChromaGamerBase) {
                val ourData = us.commonUserData.playerData
                // Iterate through the target and their connections and set all our IDs in them
                for (theirOtherClass in registeredClasses) {
                    // Skip our own class because we have the IDs
                    if (theirOtherClass == javaClass) continue
                    val theirConnected = them.getAs(theirOtherClass) ?: continue
                    val theirConnectedData = theirConnected.commonUserData.playerData
                    // Set new IDs
                    for (ourOtherFolder in registeredFolders) {
                        if (ourData.contains(ourOtherFolder + "_id")) {
                            theirConnectedData[ourOtherFolder + "_id"] =
                                ourData.getString(ourOtherFolder + "_id") // Set all existing IDs
                        }
                    }
                    theirConnected.config.signalChange()
                }
            }
            sync(this, user)
            sync(user, this)
        }
    }

    /**
     * Returns the ID for the T typed player object connected with this one or null if no connection found.
     *
     * @param cl The player class to get the ID from
     * @return The ID or null if not found
     */
    fun getConnectedID(cl: Class<out ChromaGamerBase>): String? {
        return getConnectedID(getStaticData(cl).folder)

    }

    /**
     * Returns the ID for the T typed player object connected with this one or null if no connection found.
     *
     * @param cl The player class to get the ID from
     * @return The ID or null if not found
     */
    fun getConnectedID(folder: String): String? {
        return commonUserData.playerData.getString(folder + "_id")
    }

    /**
     * Returns a player instance of the given type that represents the same player. This will return a new instance unless the player is cached.<br></br>
     * If the class is a subclass of the current class then the same ID is used, otherwise, a connected ID is used, if found.
     *
     * @param cl The target player class
     * @return The player as a [T] object or null if the user doesn't have an account there
     */
    fun <T : ChromaGamerBase> getAs(cl: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        if (cl.simpleName == javaClass.simpleName) return this as T
        val data = getStaticData(cl)
        if (data.folder == folder) // If in the same folder, the same filename is used
            return getUser(fileName, cl)
        return getConnectedID(data.folder)?.let { getUser(it, cl) }
    }

    /**
     * Returns the filename for this player data. For example, for Minecraft-related data, MC UUIDs, for Discord data, Discord IDs, etc.<br></br>
     * **Does not include .yml**
     */
    val fileName: String by lazy {
        commonUserData.playerData.getString(folder + "_id") ?: throw RuntimeException("ID not set!")
    }

    /**
     * Returns the folder that this player data is stored in. For example: "minecraft".
     */
    val folder: String by lazy { getStaticData(javaClass).folder }

    /**
     * Get player information. This method calls the [TBMCPlayerGetInfoEvent] to get all the player information across the TBMC plugins.
     *
     * @param target The [InfoTarget] to return the info for.
     * @return The player information.
     */
    fun getInfo(target: InfoTarget?): String {
        val event = TBMCPlayerGetInfoEvent(this, target)
        Bukkit.getServer().pluginManager.callEvent(event)
        return event.result
    }

    enum class InfoTarget {
        MCHover, MCCommand, Discord
    }

    //-----------------------------------------------------------------
    @JvmField
    val channel: ConfigData<Channel> = config.getData("channel", Channel.globalChat,
        { id ->
            getChannels().filter { ch: Channel -> ch.identifier.equals(id as String, ignoreCase = true) }
                .findAny().orElseThrow { RuntimeException("Channel $id not found!") }
        }, { ch -> ch.identifier })

    companion object {
        private const val TBMC_PLAYERS_DIR = "TBMC/players/"
        private val senderConverters = ArrayList<Function<CommandSender, out Optional<out ChromaGamerBase>>>()

        /**
         * Holds data per user class
         */
        private val staticDataMap = HashMap<Class<out ChromaGamerBase>, StaticUserData<out ChromaGamerBase>>()

        /**
         * Used for connecting with every type of user ([.connectWith]) and to init the configs.
         * Also, to construct an instance if an abstract class is provided.
         */
        @JvmStatic
        fun <T : ChromaGamerBase> registerPluginUserClass(userclass: Class<T>, constructor: Supplier<T>) {
            val prototype: Class<out T>
            val folderName: String
            if (userclass.isAnnotationPresent(UserClass::class.java)) {
                prototype = userclass
                folderName = userclass.getAnnotation(UserClass::class.java).foldername
            } else if (userclass.isAnnotationPresent(AbstractUserClass::class.java)) {
                val ucl = userclass.getAnnotation(AbstractUserClass::class.java).prototype.java
                if (!userclass.isAssignableFrom(ucl))
                    throw RuntimeException("The prototype class (${ucl.simpleName}) must be a subclass of the userclass parameter (${userclass.simpleName})!")
                @Suppress("UNCHECKED_CAST")
                prototype = ucl as Class<out T>
                folderName = userclass.getAnnotation(AbstractUserClass::class.java).foldername
            } else throw RuntimeException("Class not registered as a user class! Use @UserClass or TBMCPlayerBase")
            val sud = StaticUserData<T>(folderName)
            // Alawys register abstract and prototype class (TBMCPlayerBase and TBMCPlayer)
            sud.constructors[prototype] = constructor
            sud.constructors[userclass] = constructor
            staticDataMap[userclass] = sud
        }

        /**
         * Returns the folder name for the given player class.
         *
         * @param cl The class to get the folder from (like [TBMCPlayerBase] or one of it's subclasses)
         * @return The folder name for the given type
         * @throws RuntimeException If the class doesn't have the [UserClass] annotation.
         */
        fun <T : ChromaGamerBase> getFolderForType(cl: Class<T>): String {
            if (cl.isAnnotationPresent(UserClass::class.java))
                return cl.getAnnotation(UserClass::class.java).foldername
            else if (cl.isAnnotationPresent(AbstractUserClass::class.java))
                return cl.getAnnotation(AbstractUserClass::class.java).foldername
            throw RuntimeException("Class not registered as a user class! Use @UserClass or @AbstractUserClass")
        }

        private inline val registeredFolders: Iterable<String> get() = staticDataMap.values.map { it.folder }

        private inline val registeredClasses get() = staticDataMap.keys

        /**
         * Returns the (per-user) static data for the given player class.
         * The static data is only stored once per user class so for example
         * if you have two different [TBMCPlayerBase] classes, the static data is the same for both.
         *
         * @param cl The class to get the data from (like [TBMCPlayerBase] or one of its subclasses)
         */
        @Suppress("UNCHECKED_CAST")
        private fun <T : ChromaGamerBase> getStaticData(cl: Class<out T>) = staticDataMap.entries
            .filter { (key, _) -> key.isAssignableFrom(cl) }
            .map { (_, value) -> value as StaticUserData<T> }
            .firstOrNull()
            ?: throw RuntimeException("Class $cl not registered as a user class! Use registerUserClass()")

        /**
         * Returns the player class for the given folder name.
         *
         * @param foldername The folder to get the class from (like "minecraft")
         * @return The type for the given folder name or null if not found
         */
        @JvmStatic
        fun getTypeForFolder(foldername: String?): Class<out ChromaGamerBase>? {
            synchronized(staticDataMap) {
                return staticDataMap.filter { (_, value) -> value.folder.equals(foldername, ignoreCase = true) }
                    .map { (key, _) -> key }.singleOrNull()
            }
        }

        /***
         * Retrieves a user from cache or loads it from disk.
         *
         * @param fname Filename without .yml, the user's identifier for that type
         * @param cl User class
         * @return The user object
         */
        @JvmStatic
        @Synchronized
        fun <T : S, S : ChromaGamerBase> getUser(fname: String, cl: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            val staticUserData: StaticUserData<S> = getStaticData(cl)

            @Suppress("UNCHECKED_CAST")
            val commonUserData: CommonUserData<S> = staticUserData.userDataMap[fname]
                ?: run {
                    val folder = staticUserData.folder
                    val file = File(TBMC_PLAYERS_DIR + folder, "$fname.yml")
                    file.parentFile.mkdirs()
                    val playerData = YamlConfiguration.loadConfiguration(file)
                    playerData[staticUserData.folder + "_id"] = fname
                    CommonUserData<S>(playerData)
                }.also { staticUserData.userDataMap[fname] = it }

            return commonUserData.userCache[cl] ?: run {
                val obj = createNewUser(cl, staticUserData, commonUserData)
                commonUserData.userCache.put(obj)
                obj
            }
        }

        private fun <T : S, S : ChromaGamerBase> createNewUser(
            cl: Class<T>,
            staticUserData: StaticUserData<S>,
            commonUserData: CommonUserData<S>
        ): T {
            @Suppress("UNCHECKED_CAST")
            val obj = staticUserData.constructors[cl]?.get() as T? ?: run {
                try {
                    cl.getConstructor().newInstance()
                } catch (e: Exception) {
                    throw RuntimeException("Failed to create new instance of user of type ${cl.simpleName}!", e)
                }
            }
            obj.commonUserData = commonUserData
            obj.init()
            obj.scheduleUncache()
            return obj
        }

        /**
         * Adds a converter to the start of the list.
         *
         * @param converter The converter that returns an object corresponding to the sender or null, if it's not the right type.
         */
        @JvmStatic
        fun addConverter(converter: Function<CommandSender, Optional<out ChromaGamerBase>>) {
            senderConverters.add(0, converter)
        }

        /**
         * Get from the given sender. the object's type will depend on the sender's type. May be null, but shouldn't be.
         *
         * @param sender The sender to use
         * @return A user as returned by a converter or null if none can supply it
         */
        @JvmStatic
        fun getFromSender(sender: CommandSender): ChromaGamerBase? { // TODO: Use Command2Sender
            for (converter in senderConverters) {
                val ocg = converter.apply(sender)
                if (ocg.isPresent) return ocg.get()
            }
            return null
        }

        fun saveUsers() {
            synchronized(staticDataMap) {
                for (sud in staticDataMap.values) for (cud in sud.userDataMap.values) saveNow(cud.playerData) //Calls save()
            }
        }
    }
}