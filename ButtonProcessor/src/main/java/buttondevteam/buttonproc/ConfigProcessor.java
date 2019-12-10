package buttondevteam.buttonproc;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;

public class ConfigProcessor {
	private final ProcessingEnvironment procEnv;
	private final YamlConfiguration yc = new YamlConfiguration();
	private final FileObject fo;

	public ConfigProcessor(ProcessingEnvironment procEnv) {
		FileObject fo1;
		this.procEnv = procEnv;
		try {
			fo1 = procEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "configHelp.yml");
		} catch (IOException e) {
			e.printStackTrace();
			fo1 = null;
		}
		this.fo = fo1;
	}

	public void process(Element targetcl) {
		if (targetcl.getModifiers().contains(Modifier.ABSTRACT)) return;
		HasConfig hasConfig = targetcl.getAnnotation(HasConfig.class);
		if (hasConfig == null) {
			System.out.println("That's not our HasConfig annotation...");
			return;
		}
		final String path = hasConfig.global() ? "global" : "components." + targetcl.getSimpleName();
		File file = new File(fo.toUri());
		try {
			if (file.exists())
				yc.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		for (Element e : targetcl.getEnclosedElements()) {
			/*System.out.println("Element: "+e);
			System.out.println("Type: "+e.getClass()+" - "+e.getKind());
			if(e instanceof ExecutableElement)
				System.out.println("METHOD!");*/
			if (!(e instanceof ExecutableElement)) continue;
			TypeMirror tm = ((ExecutableElement) e).getReturnType();
			if (tm.getKind() != TypeKind.DECLARED) continue;
			DeclaredType dt = (DeclaredType) tm;
			if (!dt.asElement().getSimpleName().contentEquals("ConfigData"))
				continue; //Ahhha! There was a return here! (MinecraftChatModule getListener())
			System.out.println("Config: " + e.getSimpleName());

			String doc = procEnv.getElementUtils().getDocComment(e);
			if (doc == null) continue;
			System.out.println("DOC: " + doc);
			yc.set(path + "." + e.getSimpleName(), doc.trim());
		}
		String javadoc = procEnv.getElementUtils().getDocComment(targetcl);
		if (javadoc != null) {
			System.out.println("JAVADOC");
			System.out.println(javadoc.trim());
			yc.set(path, javadoc.trim());
		}
		try {
			yc.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
