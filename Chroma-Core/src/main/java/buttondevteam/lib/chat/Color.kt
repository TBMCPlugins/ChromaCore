package buttondevteam.lib.chat

enum class Color(private val cname: String, val red: Int, val green: Int, val blue: Int) : TellrawSerializableEnum {
    Black("black", 0, 0, 0),
    DarkBlue("dark_blue", 0, 0, 170),
    DarkGreen("dark_green", 0, 170, 0),
    DarkAqua("dark_aqua", 0, 170, 170),
    DarkRed("dark_red", 170, 0, 0),
    DarkPurple("dark_purple", 0, 170, 0),
    Gold("gold", 255, 170, 0),
    Gray("gray", 170, 170, 170),
    DarkGray("dark_gray", 85, 85, 85),
    Blue("blue", 85, 85, 255),
    Green("green", 85, 255, 85),
    Aqua("aqua", 85, 255, 255),
    Red("red", 255, 85, 85),
    LightPurple("light_purple", 255, 85, 255),
    Yellow("yellow", 255, 255, 85),
    White("white", 255, 255, 255);

    override fun getName(): String {
        return cname
    }
}