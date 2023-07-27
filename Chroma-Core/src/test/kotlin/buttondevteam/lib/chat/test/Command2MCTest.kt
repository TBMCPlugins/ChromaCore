package buttondevteam.lib.chat.test

import be.seeseemelk.mockbukkit.MockBukkit
import buttondevteam.core.MainPlugin
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.chat.commands.CommandUtils.coreExecutable
import buttondevteam.lib.chat.test.Command2MCCommands.*
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
        TestCommand.register()
        val nodes = ButtonPlugin.command2MC.commandNodes
        assert(nodes.size == 1)
        assert(nodes.first().literal == "test")
        val coreExecutable = nodes.first().coreExecutable<Command2MCSender, TestCommand>()
        assertEquals(TestCommand::class.qualifiedName, coreExecutable?.data?.command?.let { it::class.qualifiedName }, "The command class name doesn't match or command is null")
        assertEquals("test", coreExecutable?.data?.argumentsInOrder?.firstOrNull()?.name, "Failed to get correct argument name")
        assertEquals(String::class.java, coreExecutable?.data?.arguments?.get("test")?.type, "The argument could not be found or type doesn't match")
        assertEquals(Command2MCSender::class.java, coreExecutable?.data?.senderType, "The sender's type doesn't seem to be stored correctly")

        NoArgTestCommand.register()
        val errCmd = ErroringTestCommand
        assertEquals("No sender parameter for method '${errCmd::class.java.getMethod("def")}'", assertFails { errCmd.register() }.message)
        MultiArgTestCommand.register()

        TestNoMainCommand1.register()
        TestNoMainCommand2.register()
        TestParamsCommand.register()
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
            private var messageReceived: String = ""
            private var allowMessageReceive = false

            override fun sendMessage(message: String) {
                if (allowMessageReceive) {
                    messageReceived += message + "\n"
                } else {
                    error(message)
                }
            }

            override fun sendMessage(message: Array<String>) {
                sendMessage(message.joinToString("\n"))
            }

            fun withMessageReceive(action: () -> Unit): String {
                messageReceived = ""
                allowMessageReceive = true
                action()
                allowMessageReceive = false
                return messageReceived.trim()
            }

            fun runCommand(command: String, obj: ITestCommand2MC, expected: String) {
                assert(ButtonPlugin.command2MC.handleCommand(this, command)) { "Could not find command $command" }
                assertEquals(expected, obj.testCommandReceived)
            }

            fun runCommandWithReceive(command: String): String {
                return withMessageReceive { ButtonPlugin.command2MC.handleCommand(this, command) }
            }
        }
        sender.runCommand("/test hmm", TestCommand, "hmm")
        sender.runCommand("/noargtest", NoArgTestCommand, "TestPlayer")
        assertFails { ButtonPlugin.command2MC.handleCommand(sender, "/noargtest failing") }
        runFailingCommand(sender, "/erroringtest")
        sender.runCommand("/multiargtest test hmm mhm", MultiArgTestCommand, "hmmmhm")
        sender.runCommand("/multiargtest test2 true 19", MultiArgTestCommand, "true 19")

        sender.runCommand("/multiargtest testoptional", MultiArgTestCommand, "false")
        sender.runCommand("/multiargtest testoptional true", MultiArgTestCommand, "true")
        sender.runCommand("/multiargtest testoptionalmulti true teszt", MultiArgTestCommand, "true teszt")
        sender.runCommand("/multiargtest testoptionalmulti true", MultiArgTestCommand, "true null")
        sender.runCommand("/multiargtest testoptionalmulti", MultiArgTestCommand, "false null")

        sender.runCommand("/test plugin Chroma-Core", TestCommand, "Chroma-Core")
        assertFails { ButtonPlugin.command2MC.handleCommand(sender, "/test playerfail TestPlayer") }

        assertEquals("Test command\n" +
            "Used for testing\n" +
            "§6---- Subcommands ----\n" +
            "/test playerfail\n" +
            "/test plugin", sender.runCommandWithReceive("/test")
        )

        sender.runCommand("/some test cmd", TestNoMainCommand1, "TestPlayer")
        sender.runCommand("/some another cmd", TestNoMainCommand2, "TestPlayer")

        assertEquals("§6---- Subcommands ----\n" +
            "/some another cmd\n" +
            "/some test cmd", sender.runCommandWithReceive("/some")
        )

        sender.runCommand("/testparams 12 34 56 78", TestParamsCommand, "12 34 56.0 78.0 Player0")
        assertEquals("§cExpected integer at position 11: ...estparams <--[HERE]", sender.runCommandWithReceive("/testparams asd 34 56 78"))
        // TODO: Change test when usage help is added
    }

    private fun ICommand2MC.register() {
        MainPlugin.instance.registerCommand(this)
    }

    private fun runFailingCommand(sender: Command2MCSender, command: String) {
        assert(!ButtonPlugin.command2MC.handleCommand(sender, command)) { "Could execute command $command that shouldn't work" }
    }

    companion object {
        private var initialized = false
    }

}