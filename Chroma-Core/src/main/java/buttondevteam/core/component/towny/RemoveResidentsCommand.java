package buttondevteam.core.component.towny;

import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.CustomTabComplete;
import buttondevteam.lib.chat.ICommand2MC;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyObject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.AbstractMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandClass(path = "chroma remresidents", modOnly = true, helpText = {
	"Removes invalid Towny residents from their towns (usually after a rename that didn't get caught by the plugin)",
	"If the delete eco account setting is off, then it will completely delete the resident",
	"(The economy account could still be used by the player)"
})
public class RemoveResidentsCommand extends ICommand2MC {
	@Command2.Subcommand
	public void def(CommandSender sender, @Command2.OptionalArg @CustomTabComplete("remove") String remove) {
		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
			sender.sendMessage("Starting...");
			var ds = TownyUniverse.getInstance().getDataSource();
			var res = ds.getResidents().stream()
				.flatMap(r -> Stream.of(r) //https://stackoverflow.com/questions/37299312/in-java-8-lambdas-how-to-access-original-object-in-the-stream
					.map(TownyObject::getName).map(Bukkit::getOfflinePlayer).filter(p -> !p.hasPlayedBefore())
					.map(p -> new AbstractMap.SimpleEntry<>(r, p)))
				.collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
			sender.sendMessage("Residents to remove:");
			res.values().forEach(op -> sender.sendMessage(op.getName()));
			if (TownySettings.isDeleteEcoAccount())
				sender.sendMessage("§bWill only remove from town, as delete eco account setting is on");
			else
				sender.sendMessage("§eWill completely delete the resident, delete eco account setting is off");
			if (remove != null && remove.equalsIgnoreCase("remove")) {
				sender.sendMessage("Removing residents..."); //Removes from town and deletes town if needed - doesn't delete the resident if the setting is on
				//If it did, that could mean the player's economy is deleted too, unless this setting is false
				res.keySet().forEach(TownySettings.isDeleteEcoAccount() ? ds::removeResident : ds::removeResidentList);
				sender.sendMessage("Done");
			}
		});
	}
}
