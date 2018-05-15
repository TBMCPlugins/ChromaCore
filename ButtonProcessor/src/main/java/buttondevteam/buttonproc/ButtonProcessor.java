package buttondevteam.buttonproc;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

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
                System.out.println("Annotations: " + annotationMirrors);
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
                    System.out.println("Type: " + type);
                }
            }
        }
        return true; // claim the annotations
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
