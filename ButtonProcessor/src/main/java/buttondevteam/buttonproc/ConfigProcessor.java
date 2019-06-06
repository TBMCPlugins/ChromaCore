package buttondevteam.buttonproc;

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
import java.io.FileWriter;
import java.io.IOException;

public class ConfigProcessor {
	private final ProcessingEnvironment procEnv;
	private final FileWriter sw;

	public ConfigProcessor(ProcessingEnvironment procEnv) {
		this.procEnv = procEnv;
		FileWriter sw = null;
		try {
			FileObject file = procEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "configHelp.md");
			sw = new FileWriter(new File(file.toUri()));
			System.out.println(file.toUri());
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sw = sw;
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
			if (!dt.asElement().getSimpleName().contentEquals("ConfigData"))
				continue; //Ahhha! There was a return here! (MinecraftChatModule getListener())
			System.out.println("Config: " + e.getSimpleName());

			String doc = procEnv.getElementUtils().getDocComment(e);
			if (doc == null) continue;
			System.out.println("DOC: " + doc);
			try {
				sw.append(path).append(".").append(String.valueOf(e.getSimpleName())).append(System.lineSeparator()).append(System.lineSeparator());
				sw.append(doc.trim()).append(System.lineSeparator()).append(System.lineSeparator());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		String javadoc = procEnv.getElementUtils().getDocComment(targetcl);
		try {
			if (javadoc != null) {
				System.out.println("JAVADOC");
				System.out.println(javadoc.trim());
				sw.append(path).append(System.lineSeparator()).append(System.lineSeparator());
				sw.append(javadoc).append(System.lineSeparator()).append(System.lineSeparator());
			}
			sw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		sw.close();
		super.finalize();
	}
}
