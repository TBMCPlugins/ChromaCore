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
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

@ChromaGamerEnforcer
abstract class ChromaGamerBase {
    lateinit var config: IHaveConfig

    @JvmField
    protected var commonUserData: CommonUserData<*>? = null
    protected open fun init() {
        config.reset(commonUserData!!.playerData)
    }

    protected fun updateUserConfig() {}

    /**
     * Saves the player. It'll handle all exceptions that may happen. Called automatically.
     */
    protected open fun save() {
        try {
            if (commonUserData!!.playerData.getKeys(false).size > 0) commonUserData!!.playerData.save(
                File(
                    TBMC_PLAYERS_DIR + folder, fileName + ".yml"
                )
            )
        } catch (e: Exception) {
            TBMCCoreAPI.SendException(
                "Error while saving player to " + folder + "/" + fileName + ".yml!",
                e,
                MainPlugin.instance
            )
        }
    }

    /**
     * Removes the user from the cache. This will be called automatically after some time by default.
     */
    fun uncache() {
        val userCache: HashMap<Class<out Any?>, out ChromaGamerBase> = commonUserData!!.userCache
        synchronized(userCache) { if (userCache.containsKey(javaClass)) check(userCache.remove(javaClass) === this) { "A different player instance was cached!" } }
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
    fun <T : ChromaGamerBase?> connectWith(user: T) {
        // Set the ID, go through all linked files and connect them as well
        val ownFolder = folder
        val userFolder = user!!.folder
        if (ownFolder.equals(
                userFolder,
                ignoreCase = true
            )
        ) throw RuntimeException("Do not connect two accounts of the same type! Type: $ownFolder")
        val ownData = commonUserData!!.playerData
        val userData = user.commonUserData!!.playerData
        userData[ownFolder + "_id"] = ownData.getString(ownFolder + "_id")
        ownData[userFolder + "_id"] = userData.getString(userFolder + "_id")
        config.signalChange()
        user.config.signalChange()
        val sync = Consumer { sourcedata: YamlConfiguration ->
            val sourcefolder = if (sourcedata === ownData) ownFolder else userFolder
            val id = sourcedata.getString(sourcefolder + "_id")!!
            for ((key, value) in staticDataMap) { // Set our ID in all files we can find, both from our connections and the new ones
                if (key == javaClass || key == user.javaClass) continue
                val entryFolder = value.folder
                val otherid = sourcedata.getString(entryFolder + "_id") ?: continue
                val cg = getUser(otherid, key)!!
                val cgData = cg.commonUserData!!.playerData
                cgData[sourcefolder + "_id"] = id // Set new IDs
                for ((_, value1) in staticDataMap) {
                    val itemFolder = value1.folder
                    if (sourcedata.contains(itemFolder + "_id")) {
                        cgData[itemFolder + "_id"] = sourcedata.getString(itemFolder + "_id") // Set all existing IDs
                    }
                }
                cg.config.signalChange()
            }
        }
        sync.accept(ownData)
        sync.accept(userData)
    }

    /**
     * Returns the ID for the T typed player object connected with this one or null if no connection found.
     *
     * @param cl The player class to get the ID from
     * @return The ID or null if not found
     */
    fun <T : ChromaGamerBase?> getConnectedID(cl: Class<T>): String {
        return commonUserData!!.playerData.getString(getFolderForType(cl) + "_id")!!
    }

    /**
     * Returns a player instance of the given type that represents the same player. This will return a new instance unless the player is cached.<br></br>
     * If the class is a subclass of the current class then the same ID is used, otherwise, a connected ID is used, if found.
     *
     * @param cl The target player class
     * @return The player as a [T] object or null if the user doesn't have an account there
     */
    fun <T : ChromaGamerBase?> getAs(cl: Class<T>): T? {
        if (cl.simpleName == javaClass.simpleName) return this as T
        val newfolder = getFolderForType(cl)
            ?: throw RuntimeException("The specified class " + cl.simpleName + " isn't registered!")
        if (newfolder == folder) // If in the same folder, the same filename is used
            return getUser(fileName, cl)
        val playerData = commonUserData!!.playerData
        return if (!playerData.contains(newfolder + "_id")) null else getUser(
            playerData.getString(newfolder + "_id")!!,
            cl
        )
    }

    val fileName: String
        /**
         * This method returns the filename for this player data. For example, for Minecraft-related data, MC UUIDs, for Discord data, Discord IDs, etc.<br></br>
         * **Does not include .yml**
         */
        get() = commonUserData!!.playerData.getString(folder + "_id")!!
    val folder: String
        /**
         * This method returns the folder that this player data is stored in. For example: "minecraft".
         */
        get() = getFolderForType(javaClass)

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
    val channel: ConfigData<Channel> = config.getData("channel", Channel.GlobalChat,
        { id ->
            getChannels().filter { ch: Channel -> ch.identifier.equals(id as String, ignoreCase = true) }
                .findAny().orElse(null)
        }) { ch -> ch.ID }

    companion object {
        private const val TBMC_PLAYERS_DIR = "TBMC/players/"
        private val senderConverters = ArrayList<Function<CommandSender, out Optional<out ChromaGamerBase>>>()

        /**
         * Holds data per user class
         */
        private val staticDataMap = HashMap<Class<out ChromaGamerBase>, StaticUserData<*>>()

        /**
         * Used for connecting with every type of user ([.connectWith]) and to init the configs.
         * Also, to construct an instance if an abstract class is provided.
         */
        @JvmStatic
        fun <T : ChromaGamerBase?> RegisterPluginUserClass(userclass: Class<T>, constructor: Supplier<T>?) {
            val cl: Class<out T>
            val folderName: String
            if (userclass.isAnnotationPresent(UserClass::class.java)) {
                cl = userclass
                folderName = userclass.getAnnotation(UserClass::class.java).foldername
            } else if (userclass.isAnnotationPresent(AbstractUserClass::class.java)) {
                val ucl: Class<out ChromaGamerBase> = userclass.getAnnotation(
                    AbstractUserClass::class.java
                ).prototype
                if (!userclass.isAssignableFrom(ucl)) throw RuntimeException("The prototype class (" + ucl.simpleName + ") must be a subclass of the userclass parameter (" + userclass.simpleName + ")!")
                cl = ucl as Class<out T>
                folderName = userclass.getAnnotation(AbstractUserClass::class.java).foldername
            } else throw RuntimeException("Class not registered as a user class! Use @UserClass or TBMCPlayerBase")
            val sud = StaticUserData<T>(folderName)
            sud.constructors[cl] = constructor
            sud.constructors[userclass] =
                constructor // Alawys register abstract and prototype class (TBMCPlayerBase and TBMCPlayer)
            staticDataMap[userclass] = sud
        }

        /**
         * Returns the folder name for the given player class.
         *
         * @param cl The class to get the folder from (like [TBMCPlayerBase] or one of it's subclasses)
         * @return The folder name for the given type
         * @throws RuntimeException If the class doesn't have the [UserClass] annotation.
         */
        fun <T : ChromaGamerBase?> getFolderForType(cl: Class<T>): String {
            if (cl.isAnnotationPresent(UserClass::class.java)) return cl.getAnnotation(UserClass::class.java).foldername else if (cl.isAnnotationPresent(
                    AbstractUserClass::class.java
                )
            ) return cl.getAnnotation(AbstractUserClass::class.java).foldername
            throw RuntimeException("Class not registered as a user class! Use @UserClass or @AbstractUserClass")
        }

        /**
         * Returns the player class for the given folder name.
         *
         * @param foldername The folder to get the class from (like "minecraft")
         * @return The type for the given folder name or null if not found
         */
        fun getTypeForFolder(foldername: String?): Class<out ChromaGamerBase> {
            synchronized(staticDataMap) {
                return staticDataMap.entries.stream()
                    .filter { (_, value): Map.Entry<Class<out ChromaGamerBase>, StaticUserData<*>> ->
                        value.folder.equals(
                            foldername,
                            ignoreCase = true
                        )
                    }
                    .map { (key, value) -> java.util.Map.Entry.key }.findAny().orElse(null)
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
        fun <T : ChromaGamerBase> getUser(fname: String, cl: Class<T>): T {
            val staticUserData: StaticUserData<*> = staticDataMap.entries
                .filter { (key, _) -> key.isAssignableFrom(cl) }
                .map { (_, value) -> value }
                .firstOrNull()
                ?: throw RuntimeException("User class not registered! Use @UserClass or @AbstractUserClass")

            @Suppress("UNCHECKED_CAST")
            val commonUserData: CommonUserData<T> = (staticUserData.userDataMap[fname]
                ?: run {
                    val folder = staticUserData.folder
                    val file = File(TBMC_PLAYERS_DIR + folder, "$fname.yml")
                    file.parentFile.mkdirs()
                    val playerData = YamlConfiguration.loadConfiguration(file)
                    playerData[staticUserData.folder + "_id"] = fname
                    CommonUserData<T>(playerData)
                }.also { staticUserData.userDataMap[fname] = it }) as CommonUserData<T>

            return if (commonUserData.userCache.containsKey(cl)) commonUserData.userCache[cl] as T
            else {
                val obj = createNewUser(cl, staticUserData, commonUserData)
                commonUserData.userCache[cl] = obj
                obj
            }
        }

        private fun <T : ChromaGamerBase> createNewUser(
            cl: Class<T>,
            staticUserData: StaticUserData<*>,
            commonUserData: CommonUserData<*>
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
        fun <T : ChromaGamerBase?> addConverter(converter: Function<CommandSender, Optional<T>>) {
            senderConverters.add(0, converter)
        }

        /**
         * Get from the given sender. the object's type will depend on the sender's type. May be null, but shouldn't be.
         *
         * @param sender The sender to use
         * @return A user as returned by a converter or null if none can supply it
         */
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