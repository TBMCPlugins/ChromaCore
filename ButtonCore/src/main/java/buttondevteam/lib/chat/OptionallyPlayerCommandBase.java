package buttondevteam.lib.chat;

import buttondevteam.lib.TBMCCoreAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class OptionallyPlayerCommandBase extends TBMCCommandBase {
	public boolean OnCommand(Player player, String alias, String[] args) {
		if (getClass().isAnnotationPresent(OptionallyPlayerCommandClass.class)
				&& getClass().getAnnotation(OptionallyPlayerCommandClass.class).playerOnly())
			TBMCCoreAPI.SendException("Error while executing command " + getClass().getSimpleName() + "!",
					new Exception(
							"The PlayerCommand annotation is present and set to playerOnly but the Player overload isn't overriden!"));
		return true;
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (sender instanceof Player)
			return OnCommand((Player) sender, alias, args);
		if (!getClass().isAnnotationPresent(OptionallyPlayerCommandClass.class)
				|| !getClass().getAnnotation(OptionallyPlayerCommandClass.class).playerOnly())
			TBMCCoreAPI.SendException("Error while executing command " + getClass().getSimpleName() + "!",
					new Exception(
							"Command class doesn't override the CommandSender overload and no PlayerCommandClass annotation is present or playerOnly is false!"));
		sender.sendMessage("§cYou need to be a player to use this command.");
		return true;
	}
}
