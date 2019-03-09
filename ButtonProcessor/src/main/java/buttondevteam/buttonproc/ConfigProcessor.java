package buttondevteam.buttonproc;

import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;

public class ConfigProcessor {
	private final ProcessingEnvironment procEnv;
	private final YamlConfiguration yaml;
	private final File file;

	public ConfigProcessor(ProcessingEnvironment procEnv) {
		this.procEnv = procEnv;
		FileObject file = null;
		try {
			file = procEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "config.yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
		yaml = new YamlConfiguration();
		this.file = new File(file.toUri());
	}

	public void process(Element targetcl) {
		if (targetcl.getModifiers().contains(Modifier.ABSTRACT)) return;
		String javadoc = procEnv.getElementUtils().getDocComment(targetcl);
		if (javadoc == null) return;
		System.out.println("JAVADOC"); //TODO: Config methods
		System.out.println(javadoc);
		yaml.set("components." + targetcl.getSimpleName() + "._doc", javadoc);
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
