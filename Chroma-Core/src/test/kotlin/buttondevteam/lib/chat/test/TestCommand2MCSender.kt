package buttondevteam.lib.chat.test

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.player.TBMCPlayer
import buttondevteam.lib.test.TestCommandFailedException
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TestCommand2MCSender(user: TBMCPlayer) : Command2MCSender(user, Channel.globalChat, user) {
    private var messageReceived: String = ""
    private var allowMessageReceive = false

    override fun sendMessage(message: String) {
        if (allowMessageReceive) {
            messageReceived += message + "\n"
        } else {
            error("Received unexpected message during test: $message")
        }
    }

    override fun sendMessage(message: Array<String>) {
        sendMessage(message.joinToString("\n"))
    }

    private fun withMessageReceive(action: () -> Unit): String {
        messageReceived = ""
        allowMessageReceive = true
        val result = kotlin.runCatching { action() }
        allowMessageReceive = false
        result.getOrThrow()
        return messageReceived.trim()
    }

    private fun runCommand(command: String): Boolean {
        return ButtonPlugin.command2MC.handleCommand(this, command)
    }

    private fun assertCommand(command: String) {
        assert(runCommand(command)) { "Could not find command $command" }
    }

    /**
     * Asserts that the command could be found and the command sets the appropriate property to the expected value.
     */
    fun assertCommand(command: String, obj: Command2MCCommands.ITestCommand2MC, expected: String) {
        assertCommand(command)
        assertEquals(expected, obj.testCommandReceived)
    }

    /**
     * Runs the command and returns any value sent to the sender.
     */
    @Deprecated("This isn't an assertion function, use the assertion ones instead.")
    fun runCommandWithReceive(command: String): String {
        return withMessageReceive { runCommand(command) }
    }

    /**
     * Tests for when the command either runs successfully and sends back some text or displays help text.
     */
    fun assertCommandReceiveMessage(command: String, expectedMessage: String) {
        assertEquals(expectedMessage, withMessageReceive { assertCommand(command) })
    }

    /**
     * Tests for when the command cannot be executed at all.
     */
    fun assertFailingCommand(command: String) {
        assert(!runCommand(command)) { "Could execute command $command that shouldn't work" }
    }

    /**
     * Tests for when the command execution encounters an exception. This includes test exceptions as well.
     */
    fun assertCrashingCommand(command: String, errorCheck: (Throwable) -> Boolean) {
        val ex = assertFails { runCommand(command) }
        assert(errorCheck(ex)) { "Command exception failed test! Exception: ${ex.stackTraceToString()}" }
    }

    /**
     * Tests for expected user errors. Anything that results in the command system (and not the command itself) sending anything to the user.
     */
    fun assertCommandUserError(command: String, message: String) {
        assertCrashingCommand(command) { ex -> ex.cause?.let { it is TestCommandFailedException && it.message == message } ?: false }
    }
}