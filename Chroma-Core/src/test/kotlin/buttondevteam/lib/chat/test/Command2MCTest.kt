package buttondevteam.lib.chat.test

import be.seeseemelk.mockbukkit.MockBukkit
import buttondevteam.core.MainPlugin
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.chat.commands.CommandUtils.coreExecutable
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayer
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFails

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
        MainPlugin.instance.registerCommand(Command2MCCommands.TestCommand)
        val nodes = ButtonPlugin.command2MC.commandNodes
        assert(nodes.size == 1)
        assert(nodes.first().literal == "test")
        val coreExecutable = nodes.first().coreExecutable<Command2MCSender, Command2MCCommands.TestCommand>()
        assertEquals(Command2MCCommands.TestCommand::class.qualifiedName, coreExecutable?.data?.command?.let { it::class.qualifiedName }, "The command class name doesn't match or command is null")
        assertEquals("test", coreExecutable?.data?.argumentsInOrder?.firstOrNull()?.name, "Failed to get correct argument name")
        assertEquals(String::class.java, coreExecutable?.data?.arguments?.get("test")?.type, "The argument could not be found or type doesn't match")
        assertEquals(Command2MCSender::class.java, coreExecutable?.data?.senderType, "The sender's type doesn't seem to be stored correctly")

        MainPlugin.instance.registerCommand(Command2MCCommands.NoArgTestCommand)
        assertEquals("No sender parameter for method '${Command2MCCommands.ErroringTestCommand::class.java.getMethod("def")}'", assertFails { MainPlugin.instance.registerCommand(Command2MCCommands.ErroringTestCommand) }.message)
        MainPlugin.instance.registerCommand(Command2MCCommands.MultiArgTestCommand)

        MainPlugin.instance.registerCommand(Command2MCCommands.TestNoMainCommand1)
        MainPlugin.instance.registerCommand(Command2MCCommands.TestNoMainCommand2)
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
        user.playerName = "TestPlayer"
        val sender = object : Command2MCSender(user, Channel.globalChat, user) {
            private var messageReceived: String? = null
            private var allowMessageReceive = false

            override fun sendMessage(message: String) {
                if (allowMessageReceive) {
                    messageReceived = message
                } else {
                    error(message)
                }
            }

            override fun sendMessage(message: Array<String>) {
                sendMessage(message.joinToString("\n"))
            }

            fun withMessageReceive(action: () -> Unit): String? {
                allowMessageReceive = true
                action()
                allowMessageReceive = false
                return messageReceived
            }
        }
        runCommand(sender, "/test hmm", Command2MCCommands.TestCommand, "hmm")
        runCommand(sender, "/noargtest", Command2MCCommands.NoArgTestCommand, "TestPlayer")
        assertFails { ButtonPlugin.command2MC.handleCommand(sender, "/noargtest failing") }
        runFailingCommand(sender, "/erroringtest")
        runCommand(sender, "/multiargtest test hmm mhm", Command2MCCommands.MultiArgTestCommand, "hmmmhm")
        runCommand(sender, "/multiargtest test2 true 19", Command2MCCommands.MultiArgTestCommand, "true 19")

        runCommand(sender, "/multiargtest testoptional", Command2MCCommands.MultiArgTestCommand, "false")
        runCommand(sender, "/multiargtest testoptional true", Command2MCCommands.MultiArgTestCommand, "true")
        runCommand(sender, "/multiargtest testoptionalmulti true teszt", Command2MCCommands.MultiArgTestCommand, "true teszt")
        runCommand(sender, "/multiargtest testoptionalmulti true", Command2MCCommands.MultiArgTestCommand, "true null")
        runCommand(sender, "/multiargtest testoptionalmulti", Command2MCCommands.MultiArgTestCommand, "false null")

        runCommand(sender, "/test plugin Chroma-Core", Command2MCCommands.TestCommand, "Chroma-Core")
        assertFails { ButtonPlugin.command2MC.handleCommand(sender, "/test playerfail TestPlayer") }

        assertEquals("Test command\n" +
            "Used for testing\n" +
            "§6---- Subcommands ----\n" +
            "/test playerfail\n" +
            "/test plugin", sender.withMessageReceive { ButtonPlugin.command2MC.handleCommand(sender, "/test") })

        runCommand(sender, "/some test cmd", Command2MCCommands.TestNoMainCommand1, "TestPlayer")
        runCommand(sender, "/some another cmd", Command2MCCommands.TestNoMainCommand2, "TestPlayer")

        assertEquals("§6---- Subcommands ----\n" +
            "/some test cmd\n" +
            "/some another cmd", sender.withMessageReceive { ButtonPlugin.command2MC.handleCommand(sender, "/some") })
    }

    private fun runCommand(sender: Command2MCSender, command: String, obj: Command2MCCommands.ITestCommand2MC, expected: String) {
        assert(ButtonPlugin.command2MC.handleCommand(sender, command)) { "Could not find command $command" }
        assertEquals(expected, obj.testCommandReceived)
    }

    private fun runFailingCommand(sender: Command2MCSender, command: String) {
        assert(!ButtonPlugin.command2MC.handleCommand(sender, command)) { "Could execute command $command that shouldn't work" }
    }

    companion object {
        private var initialized = false
    }

}