package buttondevteam.buttonproc;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("buttondevteam.*")
public class ButtonProcessor extends AbstractProcessor {
	@Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement te : annotations) {
            Set<? extends Element> classes = roundEnv.getElementsAnnotatedWith(te);
            for (Element targetcl : classes) {
                System.out.println("Processing " + targetcl);
                List<? extends AnnotationMirror> annotationMirrors = processingEnv.getElementUtils()
                        .getAllAnnotationMirrors(targetcl);
	            //System.out.println("Annotations: " + annotationMirrors);
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
                for (AnnotationMirror annotation : annotationMirrors) {
                    String type = annotation.getAnnotationType().toString();
	                //System.out.println("Type: " + type);
                }
	            processSubcommands(targetcl, annotationMirrors);
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

	private YamlConfiguration yc = new YamlConfiguration();
	private boolean found = false;

	private void processSubcommands(Element targetcl, List<? extends AnnotationMirror> annotationMirrors) {
		if (!(targetcl instanceof ExecutableElement))
			return;
		//System.out.println("Annotations: "+annotationMirrors);
		if (annotationMirrors.stream().noneMatch(an -> an.getAnnotationType().toString().endsWith("Subcommand")))
			return;
		//System.out.print("Processing method: " + targetcl.getEnclosingElement()+" "+targetcl);
		ConfigurationSection cs = yc.createSection(targetcl.getEnclosingElement().toString()
			+ "." + targetcl.getSimpleName().toString()); //Need to do the 2 config sections at once so it doesn't overwrite the class section
		System.out.println(targetcl);
		cs.set("method", targetcl.toString());
		cs.set("params", ((ExecutableElement) targetcl).getParameters().stream().skip(1).map(p -> {
			//String tn=p.asType().toString();
			//return tn.substring(tn.lastIndexOf('.')+1)+" "+p.getSimpleName();
			boolean optional = p.getAnnotationMirrors().stream().anyMatch(am -> am.getAnnotationType().toString().endsWith("Optional"));
			if (optional)
				return "[" + p.getSimpleName() + "]";
			return "<" + p.getSimpleName() + ">";
		}).collect(Collectors.joining(" ")));
		//System.out.println();
		found = true;
	}

	@Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

	private String fetchSourcePath() {
		try {
			JavaFileObject generationForPath = processingEnv.getFiler().createSourceFile("PathFor" + getClass().getSimpleName());
			Writer writer = generationForPath.openWriter();
			String sourcePath = generationForPath.toUri().getPath();
			writer.close();
			generationForPath.delete();

			return sourcePath;
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Unable to determine source file path!");
		}

		return "";
	}
}
