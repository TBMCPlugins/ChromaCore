package buttondevteam.lib.chat

//We don't need the 'extras' of CommandSender on Discord
interface Command2Sender {
    fun sendMessage(message: String)

    fun sendMessage(message: Array<String>)

    val name: String
}
