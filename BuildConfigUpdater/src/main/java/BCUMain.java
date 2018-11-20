import buttondevteam.component.updater.PluginUpdater;

import java.util.List;
import java.util.stream.Collectors;

public class BCUMain {
    public static void main(String[] args) {
        System.out.println("Getting list of repositories...");
        List<String> plugins = PluginUpdater.GetPluginNames();
        System.out.println("Removing non-Maven projects...");
        plugins.removeIf(plugin -> PluginUpdater.isNotMaven(plugin, "master"));
        System.out.println(plugins.stream().collect(Collectors.joining("\n")));
        for (String plugin : plugins) { //TODO: We don't want to apply it all at once, especially to unused/unowned repos
        } //TODO: Add it to ButtonCore - or actually as a plugin or ButtonProcessor
    }
}
