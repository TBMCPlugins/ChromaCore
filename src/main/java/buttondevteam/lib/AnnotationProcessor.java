package buttondevteam.lib;

import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import buttondevteam.lib.player.ChromaGamerEnforcer;

/** * A simple session bean type annotation processor. The implementation * is based on the standard annotation processing API in Java 6. */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {
	/** * Check if both @Stateful and @Stateless are present in an * session bean. If so, emits a warning message. */
	@Override
	public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnv) { // TODO: SEparate JAR
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ChromaGamerEnforcer.class);
		for (Element element : elements) {
			System.out.println("Processing " + element);
			List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
			System.out.println("Annotations: " + annotationMirrors);
			for (AnnotationMirror annotation : annotationMirrors) {
				String type = annotation.getAnnotationType().toString();
				System.out.println("Type: " + type);
			}
		}
		return true; // claim the annotations
	}
}
