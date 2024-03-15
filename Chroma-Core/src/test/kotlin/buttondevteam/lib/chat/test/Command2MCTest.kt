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
    @Order(1)
    fun testUnregisterCommands() {
        // First test unregistering the builtin command
        // FIXME should have the init code separate of the plugin init code
        ButtonPlugin.command2MC.unregisterCommands(MainPlugin.instance)
        assert(ButtonPlugin.command2MC.commandNodes.isEmpty())
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
    @Order(3)
    fun testHandleCommand() {
        val sender = createSender()
        sender.assertFailingCommand("/missingtest")
        sender.assertFailingCommand("/erroringtest") // Tests completely missing the sender parameter
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

    @Test
    @Order(4)
    fun testAddParamConverter() {
        TestParamConverterCommand.register()
        ButtonPlugin.command2MC.addParamConverter(TestConvertedParameter::class.java, {
            if (it == "test") null
            else TestConvertedParameter(it)
        }, "Failed to convert test param!") { arrayOf("test1", "test2").asIterable() }
        val sender = createSender()
        sender.assertCommand("/testparamconverter hmm", TestParamConverterCommand, "hmm")
        sender.assertCommandUserError("/testparamconverter test", "§cError: §cFailed to convert test param!")
    }

    @Test
    @Order(5)
    fun testHasPermission() {
    }

    private fun createSender(): TestCommand2MCSender {
        val user = ChromaGamerBase.getUser(UUID.randomUUID().toString(), TBMCPlayer::class.java)
        user.playerName = "TestPlayer"
        return TestCommand2MCSender(user)
    }

    /**
     * Tests parameter conversion, help text and errors.
     */
    private fun testTestCommand(sender: TestCommand2MCSender) {
        sender.assertCommand("/test hmm", TestCommand, "hmm")
        sender.assertCommand("/test plugin Chroma-Core", TestCommand, "Chroma-Core")
        sender.assertCrashingCommand("/test playerfail TestPlayer") { it.cause?.message == "No suitable converter found for class buttondevteam.lib.player.TBMCPlayer param1" }
        sender.assertCommandUserError("/test plugin asd", "§cError: §cNo Chroma plugin found by that name.")
        sender.assertCrashingCommand("/test errortest") { it.cause?.cause?.message === "Hmm" }
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
        sender.assertCommand("/noargtest", NoArgTestCommand, "TestPlayer")
        sender.assertCommandReceiveMessage("/noargtest failing", "") // Help text
        sender.assertCommand("/noargtest sendertest", NoArgTestCommand, "TestPlayer")
    }

    /**
     * Tests parameter type conversion with multiple (optional) parameters.
     */
    private fun testMultiArgTestCommand(sender: TestCommand2MCSender) {
        sender.assertCommand("/multiargtest test hmm mhm", MultiArgTestCommand, "hmmmhm")
        sender.assertCommand("/multiargtest test2 true 19", MultiArgTestCommand, "true 19")
        sender.assertCommand("/multiargtest testoptional", MultiArgTestCommand, "false")
        sender.assertCommand("/multiargtest testoptional true", MultiArgTestCommand, "true")
        sender.assertCommand("/multiargtest testoptionalmulti true teszt", MultiArgTestCommand, "true teszt")
        sender.assertCommand("/multiargtest testoptionalmulti true", MultiArgTestCommand, "true null")
        sender.assertCommand("/multiargtest testoptionalmulti", MultiArgTestCommand, "false null")
    }

    /**
     * Tests more type of parameters and wrong param type.
     */
    private fun testTestParamsCommand(sender: TestCommand2MCSender) {
        sender.assertCommand("/testparams 12 34 56 78", TestParamsCommand, "12 34 56.0 78.0 Player0")
        assertEquals("§cExpected integer at position 11: ...estparams <--[HERE]", sender.runCommandWithReceive("/testparams asd 34 56 78"))
        // TODO: Change test when usage help is added
    }

    /**
     * Tests a command that has no default handler.
     */
    private fun testSomeCommand(sender: TestCommand2MCSender) {
        sender.assertCommand("/some test cmd", TestNoMainCommand1, "TestPlayer")
        sender.assertCommand("/some another cmd", TestNoMainCommand2, "TestPlayer")
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
