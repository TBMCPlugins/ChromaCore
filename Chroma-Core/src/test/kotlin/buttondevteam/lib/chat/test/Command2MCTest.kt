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
        MainPlugin.instance.registerCommand(TestCommand)
        val nodes = ButtonPlugin.command2MC.commandNodes
        assert(nodes.size == 1)
        assert(nodes.first().literal == "test")
        val coreExecutable = nodes.first().coreExecutable<Command2MCSender, TestCommand>()
        assertEquals(TestCommand::class.qualifiedName, coreExecutable?.data?.command?.let { it::class.qualifiedName }, "The command class name doesn't match or command is null")
        assertEquals("test", coreExecutable?.data?.argumentsInOrder?.firstOrNull()?.name, "Failed to get correct argument name")
        assertEquals(String::class.java, coreExecutable?.data?.arguments?.get("test")?.type, "The argument could not be found or type doesn't match")
        assertEquals(Command2MCSender::class.java, coreExecutable?.data?.senderType, "The sender's type doesn't seem to be stored correctly")
        MainPlugin.instance.registerCommand(NoArgTestCommand)
        assertEquals("No sender parameter for method '${ErroringTestCommand::class.java.getMethod("def")}'", assertFails { MainPlugin.instance.registerCommand(ErroringTestCommand) }.message)
        MainPlugin.instance.registerCommand(MultiArgTestCommand)
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
            override fun sendMessage(message: String) {
                error(message)
            }

            override fun sendMessage(message: Array<String>) {
                error(message.joinToString("\n"))
            }
        }
        runCommand(sender, "/test hmm", TestCommand, "hmm")
        runCommand(sender, "/noargtest", NoArgTestCommand, "TestPlayer")
        assertFails { ButtonPlugin.command2MC.handleCommand(sender, "/noargtest failing") }
        runFailingCommand(sender, "/erroringtest")
        runCommand(sender, "/multiargtest hmm mhm", MultiArgTestCommand, "hmmmhm")
        runCommand(sender, "/multiargtest test2 true 19", MultiArgTestCommand, "true 19")
        // TODO: Add expected failed param conversions and missing params
    }

    private fun runCommand(sender: Command2MCSender, command: String, obj: ITestCommand2MC, expected: String) {
        assert(ButtonPlugin.command2MC.handleCommand(sender, command)) { "Could not find command $command" }
        assertEquals(expected, obj.testCommandReceived)
    }

    private fun runFailingCommand(sender: Command2MCSender, command: String) {
        assert(!ButtonPlugin.command2MC.handleCommand(sender, command)) { "Could execute command $command that shouldn't work" }
    }

    @CommandClass
    object TestCommand : ICommand2MC(), ITestCommand2MC {
        override var testCommandReceived: String? = null

        @Command2.Subcommand
        fun def(sender: Command2MCSender, test: String) {
            testCommandReceived = test
        }
    }

    @CommandClass
    object NoArgTestCommand : ICommand2MC(), ITestCommand2MC {
        override var testCommandReceived: String? = null

        @Command2.Subcommand
        override fun def(sender: Command2MCSender): Boolean {
            testCommandReceived = sender.name
            return true
        }

        @Command2.Subcommand
        fun failing(sender: Command2MCSender): Boolean {
            return false
        }
    }

    @CommandClass
    object ErroringTestCommand : ICommand2MC() {
        @Command2.Subcommand
        fun def() {
        }
    }

    @CommandClass
    object MultiArgTestCommand : ICommand2MC(), ITestCommand2MC {
        override var testCommandReceived: String? = null

        @Command2.Subcommand
        fun def(sender: Command2MCSender, test: String, test2: String) {
            testCommandReceived = test + test2
        }

        @Command2.Subcommand
        fun test2(sender: Command2MCSender, btest: Boolean, ntest: Int) {
            testCommandReceived = "$btest $ntest"
        }
    }

    companion object {
        private var initialized = false
    }

    interface ITestCommand2MC {
        var testCommandReceived: String?
    }
}