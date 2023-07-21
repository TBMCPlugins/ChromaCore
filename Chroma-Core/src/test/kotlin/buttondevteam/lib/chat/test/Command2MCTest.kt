package buttondevteam.lib.chat.test

import be.seeseemelk.mockbukkit.MockBukkit
import buttondevteam.core.MainPlugin
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.chat.commands.CommandUtils.coreExecutable
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayer
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.util.*
import kotlin.test.assertEquals

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
            initialized = true
        }
    }

    @Test
    @Order(2)
    fun testRegisterCommand() {
        MainPlugin.instance.registerCommand(TestCommand)
        val nodes = ButtonPlugin.command2MC.commandNodes
        assert(nodes.size == 1)
        assert(nodes.first().literal == "test")
        val coreExecutable = nodes.first().coreExecutable<Command2MCSender, TestCommand>()
        assertEquals(TestCommand::class.qualifiedName, coreExecutable?.data?.command?.let { it::class.qualifiedName }, "The command class name doesn't match or command is null")
        assertEquals("test", coreExecutable?.data?.argumentsInOrder?.firstOrNull()?.name, "Failed to get correct argument name")
        assertEquals(String::class.java, coreExecutable?.data?.arguments?.get("test")?.type, "The argument could not be found or type doesn't match")
        assertEquals(Command2MCSender::class.java, coreExecutable?.data?.senderType, "The sender's type doesn't seem to be stored correctly")
    }

    @Test
    fun testHasPermission() {
    }

    @Test
    fun testAddParamConverter() {
    }

    @Test
    @Order(1)
    fun testUnregisterCommands() {
        ButtonPlugin.command2MC.unregisterCommands(MainPlugin.instance) // FIXME should have the init code separate of the plugin init code
        assert(ButtonPlugin.command2MC.commandNodes.isEmpty())
    }

    @Test
    @Order(3)
    fun testHandleCommand() {
        val user = ChromaGamerBase.getUser(UUID.randomUUID().toString(), TBMCPlayer::class.java)
        val sender = object : Command2MCSender(user, Channel.globalChat, user) {
            override fun sendMessage(message: String) {
                error(message)
            }

            override fun sendMessage(message: Array<String>) {
                error(message.joinToString("\n"))
            }
        }
        assert(ButtonPlugin.command2MC.handleCommand(sender, "/test hmm"))
        assertEquals("hmm", testCommandReceived)
    }

    @CommandClass
    object TestCommand : ICommand2MC() {
        @Command2.Subcommand
        fun def(sender: Command2MCSender, test: String) {
            testCommandReceived = test
        }
    }

    companion object {
        private var initialized = false
        private var testCommandReceived: String? = null
    }
}