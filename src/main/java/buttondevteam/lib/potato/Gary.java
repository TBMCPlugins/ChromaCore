package buttondevteam.lib;

import java.util.List;

public class Gary extends DebugPotato {

	public Gary() {
		super.setMessage("I'M A POTATO");
		super.setType("Gary");
	}

	/**
	 * Gary has a fixed message, therefore this method has no effect.
	 */
	@Override
	public DebugPotato setMessage(List<String> message) {
		return this;
	}

	/**
	 * Gary has a fixed message, therefore this method has no effect.
	 */
	@Override
	public DebugPotato setMessage(String message) {
		return this;
	}

	/**
	 * Gary has a fixed message, therefore this method has no effect.
	 */
	@Override
	public DebugPotato setMessage(String[] message) {
		return this;
	}

	/**
	 * Gary has it's name already, therefore this method has no effect.
	 */
	@Override
	public DebugPotato setType(String type) {
		return this;
	}
}
