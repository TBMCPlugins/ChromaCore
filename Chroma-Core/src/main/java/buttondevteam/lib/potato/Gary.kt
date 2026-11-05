package buttondevteam.lib.potato

class Gary : DebugPotato() {
    init {
        super.setMessage("I'M A POTATO")
        super.setType("Gary")
    }

    /**
     * Gary has a fixed message, therefore this method has no effect.
     */
    override fun setMessage(message: List<String>): DebugPotato {
        return this
    }

    /**
     * Gary has a fixed message, therefore this method has no effect.
     */
    override fun setMessage(message: String): DebugPotato {
        return this
    }

    /**
     * Gary has a fixed message, therefore this method has no effect.
     */
    override fun setMessage(message: Array<String>): DebugPotato {
        return this
    }

    /**
     * Gary has it's name already, therefore this method has no effect.
     */
    override fun setType(type: String?): DebugPotato {
        return this
    }
}
