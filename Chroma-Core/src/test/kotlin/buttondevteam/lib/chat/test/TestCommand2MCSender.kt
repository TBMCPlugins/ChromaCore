package buttondevteam.lib.chat.test

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.player.TBMCPlayer
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TestCommand2MCSender(user: TBMCPlayer) : Command2MCSender(user, Channel.globalChat, user) {
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

    private fun withMessageReceive(action: () -> Unit): String {
        messageReceived = ""
        allowMessageReceive = true
        action()
        allowMessageReceive = false
        return messageReceived.trim()
    }

    fun runCommand(command: String, obj: Command2MCCommands.ITestCommand2MC, expected: String) {
        assert(ButtonPlugin.command2MC.handleCommand(this, command)) { "Could not find command $command" }
        assertEquals(expected, obj.testCommandReceived)
    }

    fun runCommandWithReceive(command: String): String {
        return withMessageReceive { ButtonPlugin.command2MC.handleCommand(this, command) }
    }

    fun runFailingCommand(command: String) {
        assert(!ButtonPlugin.command2MC.handleCommand(this, command)) { "Could execute command $command that shouldn't work" }
    }

    fun runCrashingCommand(command: String, errorCheck: (Throwable) -> Boolean) {
        assert(errorCheck(assertFails { ButtonPlugin.command2MC.handleCommand(this, command) })) { "Command exception failed test!" }
    }
}