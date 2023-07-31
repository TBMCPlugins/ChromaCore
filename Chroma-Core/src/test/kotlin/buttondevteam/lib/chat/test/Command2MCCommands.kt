package buttondevteam.lib.chat.test

import buttondevteam.lib.architecture.ButtonPlugin
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayer
import org.bukkit.OfflinePlayer

abstract class Command2MCCommands {
    @CommandClass(helpText = ["Test command", "Used for testing"])
    object TestCommand : ICommand2MC(), ITestCommand2MC {
        override var testCommandReceived: String? = null

        @Command2.Subcommand
        fun def(sender: Command2MCSender, test: String) {
            testCommandReceived = test
        }

        @Command2.Subcommand
        fun plugin(sender: Command2MCSender, plugin: ButtonPlugin) {
            testCommandReceived = plugin.name
        }

        @Command2.Subcommand
        fun playerFail(sender: Command2MCSender, player: TBMCPlayer) {
        }

        @Command2.Subcommand
        fun errorTest(sender: Command2MCSender) {
            error("Hmm")
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

        @Command2.Subcommand
        fun senderTest(sender: ChromaGamerBase) {
            testCommandReceived = sender.name
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
        fun test(sender: Command2MCSender, test: String, test2: String) {
            testCommandReceived = test + test2
        }

        @Command2.Subcommand
        fun test2(sender: Command2MCSender, btest: Boolean, ntest: Int) {
            testCommandReceived = "$btest $ntest"
        }

        @Command2.Subcommand
        fun testOptional(sender: Command2MCSender, @Command2.OptionalArg opt: Boolean) {
            testCommandReceived = "$opt"
        }

        @Command2.Subcommand
        fun testOptionalMulti(sender: Command2MCSender, @Command2.OptionalArg opt1: Boolean, @Command2.OptionalArg opt2: String?) {
            testCommandReceived = "$opt1 $opt2"
        }
    }

    @CommandClass(path = "some test cmd")
    object TestNoMainCommand1 : ICommand2MC(), ITestCommand2MC {
        override var testCommandReceived: String? = null

        @Command2.Subcommand
        override fun def(sender: Command2MCSender): Boolean {
            testCommandReceived = sender.name
            return true
        }
    }

    @CommandClass(path = "some another cmd")
    object TestNoMainCommand2 : ICommand2MC(), ITestCommand2MC {
        override var testCommandReceived: String? = null

        @Command2.Subcommand
        override fun def(sender: Command2MCSender): Boolean {
            testCommandReceived = sender.name
            return true
        }
    }

    @CommandClass
    object TestParamsCommand : ICommand2MC(), ITestCommand2MC {
        override var testCommandReceived: String? = null

        @Command2.Subcommand
        fun def(sender: OfflinePlayer, testInt: Int?, testLong: Long?, testDouble: Double?, testDouble2: Double) {
            testCommandReceived = "$testInt $testLong $testDouble $testDouble2 ${sender.name}"
        }
    }

    interface ITestCommand2MC {
        var testCommandReceived: String?
    }
}