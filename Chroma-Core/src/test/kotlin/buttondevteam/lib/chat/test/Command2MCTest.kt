package buttondevteam.lib.chat.test

import be.seeseemelk.mockbukkit.MockBukkit
import buttondevteam.core.MainPlugin
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayer
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.util.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class Command2MCTest {

    init {
        if (!initialized) {
            try {
                MockBukkit.mock()
            } catch (e: IllegalStateException) {
                throw RuntimeException("Failed to init tests! Something in here fails to initialize. Check the first test case.", e)
            }
            MockBukkit.load(MainPlugin::class.java, true)
            ButtonPlugin.command2MC.unregisterCommands(MainPlugin.instance) // FIXME should have the init code separate of the plugin init code
            initialized = true
        }
    }

    @Test
    @Order(1)
    fun testRegisterCommand() {
        MainPlugin.instance.registerCommand(TestCommand)
        assert(ButtonPlugin.command2MC.commandNodes.size == 1)
        assert(ButtonPlugin.command2MC.commandNodes.first().literal == "test")
    }

    @Test
    fun testHasPermission() {
    }

    @Test
    fun testAddParamConverter() {
    }

    @Test
    fun testUnregisterCommands() {
    }

    @Test
    @Order(2)
    fun testHandleCommand() {
        val user = ChromaGamerBase.getUser(UUID.randomUUID().toString(), TBMCPlayer::class.java)
        assert(ButtonPlugin.command2MC.handleCommand(Command2MCSender(user, Channel.globalChat, user), "/test hmm"))
    }

    @CommandClass
    object TestCommand : ICommand2MC() {
        @Command2.Subcommand
        fun def(sender: Command2MCSender, test: String) {
            println(test)
        }
    }

    companion object {
        private var initialized = false
    }
}