package buttondevteam.buttonproc;

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
		final String path = "components." + targetcl.getSimpleName();
		for (Element e : targetcl.getEnclosedElements()) {
			/*System.out.println("Element: "+e);
			System.out.println("Type: "+e.getClass()+" - "+e.getKind());
			if(e instanceof ExecutableElement)
				System.out.println("METHOD!");*/
			if (!(e instanceof ExecutableElement)) continue;
			TypeMirror tm = ((ExecutableElement) e).getReturnType();
			if (tm.getKind() != TypeKind.DECLARED) continue;
			DeclaredType dt = (DeclaredType) tm;
			if (!dt.asElement().getSimpleName().contentEquals("ConfigData")) return;
			System.out.println("Config: " + e.getSimpleName());
			System.out.println("Value: " + ((ExecutableElement) e).getDefaultValue());
			String doc = procEnv.getElementUtils().getDocComment(e);
			if (doc == null) continue;
			System.out.println("DOC: " + doc);
			yaml.set(path + "." + e.getSimpleName() + "_doc", doc); //methodName_doc
		}
		String javadoc = procEnv.getElementUtils().getDocComment(targetcl);
		if (javadoc == null) return;
		System.out.println("JAVADOC");
		System.out.println(javadoc);
		yaml.set(path + "._doc", javadoc);
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
