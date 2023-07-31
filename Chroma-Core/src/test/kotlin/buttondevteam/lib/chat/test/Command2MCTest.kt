package buttondevteam.lib.chat.test

import be.seeseemelk.mockbukkit.MockBukkit
import buttondevteam.core.MainPlugin
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
        assertEquals(4, nodes.size)
        assertEquals("test", nodes.first().literal)
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
        assertEquals("There are no subcommands defined in the command class TestEmptyCommand!", assertFails { TestEmptyCommand.register() }.message)
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
        val sender = TestCommand2MCSender(user)
        sender.runFailingCommand("/erroringtest") // Tests completely missing the sender parameter
        testTestCommand(sender)
        testNoArgTestCommand(sender)
        testMultiArgTestCommand(sender)
        testTestParamsCommand(sender)
        testSomeCommand(sender)
        assertEquals(
            "/multiargtest test\n" +
                "/multiargtest test2\n" +
                "/multiargtest testoptional\n" +
                "/multiargtest testoptionalmulti\n" +
                "/noargtest\n" +
                "/noargtest failing\n" +
                "/noargtest sendertest\n" +
                "/some another cmd\n" +
                "/some test cmd\n" +
                "/test\n" +
                "/test errortest\n" +
                "/test playerfail\n" +
                "/test plugin\n" +
                "/testparams", ButtonPlugin.command2MC.getCommandList(sender).joinToString("\n")
        )
    }

    /**
     * Tests parameter conversion, help text and errors.
     */
    private fun testTestCommand(sender: TestCommand2MCSender) {
        sender.runCommand("/test hmm", TestCommand, "hmm")
        sender.runCommand("/test plugin Chroma-Core", TestCommand, "Chroma-Core")
        sender.runCrashingCommand("/test playerfail TestPlayer") { it.cause?.message == "No suitable converter found for class buttondevteam.lib.player.TBMCPlayer param1" }
        assertEquals("§cError: §cNo Chroma plugin found by that name.", sender.runCommandWithReceive("/test plugin asd"))
        sender.runCrashingCommand("/test errortest") { it.cause?.cause?.message === "Hmm" }
        assertEquals(
            "Test command\n" +
                "Used for testing\n" +
                "§6---- Subcommands ----\n" +
                "/test errortest\n" +
                "/test playerfail\n" +
                "/test plugin", sender.runCommandWithReceive("/test")
        )
    }

    /**
     * Tests having no arguments for the command and different sender types.
     */
    private fun testNoArgTestCommand(sender: TestCommand2MCSender) {
        sender.runCommand("/noargtest", NoArgTestCommand, "TestPlayer")
        sender.runCrashingCommand("/noargtest failing") { it.cause?.cause is IllegalStateException }
        sender.runCommand("/noargtest sendertest", NoArgTestCommand, "TestPlayer")
    }

    /**
     * Tests parameter type conversion with multiple (optional) parameters.
     */
    private fun testMultiArgTestCommand(sender: TestCommand2MCSender) {
        sender.runCommand("/multiargtest test hmm mhm", MultiArgTestCommand, "hmmmhm")
        sender.runCommand("/multiargtest test2 true 19", MultiArgTestCommand, "true 19")
        sender.runCommand("/multiargtest testoptional", MultiArgTestCommand, "false")
        sender.runCommand("/multiargtest testoptional true", MultiArgTestCommand, "true")
        sender.runCommand("/multiargtest testoptionalmulti true teszt", MultiArgTestCommand, "true teszt")
        sender.runCommand("/multiargtest testoptionalmulti true", MultiArgTestCommand, "true null")
        sender.runCommand("/multiargtest testoptionalmulti", MultiArgTestCommand, "false null")
    }

    /**
     * Tests more type of parameters and wrong param type.
     */
    private fun testTestParamsCommand(sender: TestCommand2MCSender) {
        sender.runCommand("/testparams 12 34 56 78", TestParamsCommand, "12 34 56.0 78.0 Player0")
        assertEquals("§cExpected integer at position 11: ...estparams <--[HERE]", sender.runCommandWithReceive("/testparams asd 34 56 78"))
        // TODO: Change test when usage help is added
    }

    /**
     * Tests a command that has no default handler.
     */
    private fun testSomeCommand(sender: TestCommand2MCSender) {
        sender.runCommand("/some test cmd", TestNoMainCommand1, "TestPlayer")
        sender.runCommand("/some another cmd", TestNoMainCommand2, "TestPlayer")
        assertEquals(
            "§6---- Subcommands ----\n" +
                "/some another cmd\n" +
                "/some test cmd", sender.runCommandWithReceive("/some")
        )
    }

    private fun ICommand2MC.register() {
        MainPlugin.instance.registerCommand(this)
    }

    companion object {
        private var initialized = false
    }

}
