package buttondevteam.buttonproc;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("buttondevteam.*")
public class ButtonProcessor extends AbstractProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (configProcessor == null)
			configProcessor = new ConfigProcessor(processingEnv);
		for (TypeElement te : annotations) {
			Set<? extends Element> classes = roundEnv.getElementsAnnotatedWith(te);
			for (Element targetcl : classes) {
				List<? extends AnnotationMirror> annotationMirrors = processingEnv.getElementUtils()
					.getAllAnnotationMirrors(targetcl);
				Function<String, Boolean> hasAnnotation = ann -> annotationMirrors.stream()
					.anyMatch(am -> am.getAnnotationType().toString().contains(ann));
				if (hasAnnotation.apply("ChromaGamerEnforcer") && !hasAnnotation.apply("UserClass")
					&& !targetcl.getModifiers().contains(Modifier.ABSTRACT))
					processingEnv.getMessager().printMessage(Kind.ERROR,
						"No UserClass annotation found for " + targetcl.getSimpleName(), targetcl);
				if (hasAnnotation.apply("TBMCPlayerEnforcer") && !hasAnnotation.apply("PlayerClass")
					&& !targetcl.getModifiers().contains(Modifier.ABSTRACT))
					processingEnv.getMessager().printMessage(Kind.ERROR,
						"No PlayerClass annotation found for " + targetcl.getSimpleName(), targetcl);
				processSubcommands(targetcl, annotationMirrors);
				if (hasAnnotation.apply("HasConfig"))
					configProcessor.process(targetcl);
			}
		}
		try {
			if (found) {
				FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "commands.yml");
				yc.save(new File(fo.toUri()));
				found = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true; // claim the annotations
	}

	private final YamlConfiguration yc = new YamlConfiguration();
	private boolean found = false;
	private ConfigProcessor configProcessor;

	private void processSubcommands(Element method, List<? extends AnnotationMirror> annotationMirrors) {
		if (!(method instanceof ExecutableElement))
			return;
		if (annotationMirrors.stream().noneMatch(an -> an.getAnnotationType().toString().endsWith("Subcommand")))
			return;
		ConfigurationSection cs = yc.createSection(method.getEnclosingElement().toString()
			+ "." + method.getSimpleName().toString()); //Need to do the 2 config sections at once so it doesn't overwrite the class section
		System.out.println("Found subcommand: " + method);
		cs.set("method", method.toString());
		cs.set("params", ((ExecutableElement) method).getParameters().stream().skip(1).map(p -> {
			boolean optional = p.getAnnotationMirrors().stream().anyMatch(am -> am.getAnnotationType().toString().endsWith("OptionalArg"));
			if (optional)
				return "[" + p.getSimpleName() + "]";
			return "<" + p.getSimpleName() + ">";
		}).collect(Collectors.joining(" ")));
		found = true;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
}
