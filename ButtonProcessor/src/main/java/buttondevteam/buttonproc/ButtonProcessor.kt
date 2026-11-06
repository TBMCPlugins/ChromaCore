package buttondevteam.buttonproc

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.function.Function
import java.util.stream.Collectors
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@SupportedAnnotationTypes("buttondevteam.*")
class ButtonProcessor(val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation("buttondevteam.buttonproc.HasConfig") // TODO: Move out of the annotation processor
            .filter { it.validate() }
            .filterIsInstance<KSClassDeclaration>()
            .forEach { it.accept(ConfigClassVisitor(), Unit) }

        return emptyList()

        for (te in annotations) {
            val classes = roundEnv.getElementsAnnotatedWith(te)
            for (targetcl in classes) {
                println("TargetCL: " + targetcl.getSimpleName())
                val annotationMirrors = processingEnv.getElementUtils()
                    .getAllAnnotationMirrors(targetcl)
                val hasAnnotation: Function<String?, Boolean?> = Function { ann: String? ->
                    annotationMirrors.stream()
                        .anyMatch { am: AnnotationMirror? -> am!!.getAnnotationType().toString().contains(ann!!) }
                }
                if (hasAnnotation.apply("ChromaGamerEnforcer") && !hasAnnotation.apply("UserClass")!! && !targetcl.getModifiers().contains(Modifier.ABSTRACT)) processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "No UserClass annotation found for " + targetcl.getSimpleName(), targetcl
                )
                if (hasAnnotation.apply("TBMCPlayerEnforcer") && !hasAnnotation.apply("PlayerClass")!! && !targetcl.getModifiers().contains(Modifier.ABSTRACT)) processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "No PlayerClass annotation found for " + targetcl.getSimpleName(), targetcl
                )
                processSubcommands(targetcl, annotationMirrors)
                if (hasAnnotation.apply("HasConfig")) configProcessor!!.process(targetcl)
            }
        }
        try {
            if (found) {
                val fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "commands.yml")
                yc.save(File(fo.toUri()))
                found = false
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true // claim the annotations
    }

    class ConfigClassVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            configProcessor.process(classDeclaration)
        }
    }
}

private val yc = YamlConfiguration()
private var found = false
private var configProcessor = ConfigProcessor()

private fun processSubcommands(method: Element, annotationMirrors: MutableList<out AnnotationMirror?>) {
    println("A")
    println(method.javaClass.getSimpleName())
    if (method !is ExecutableElement) return
    println("B")
    if (annotationMirrors.stream().noneMatch { an: AnnotationMirror? -> an!!.getAnnotationType().toString().endsWith("Subcommand") }) return
    println("C")
    val cs = yc.createSection(
        (method.getEnclosingElement()
            .toString() + "." + method.getSimpleName())
    ) //Need to do the 2 config sections at once so it doesn't overwrite the class section
    println("Found subcommand: " + method)
    cs.set("method", method.toString())
    cs.set("params", method.getParameters().stream().skip(1).map<String?> { p: VariableElement? ->
        val optional = p!!.getAnnotationMirrors().stream().anyMatch { am: AnnotationMirror? -> am!!.getAnnotationType().toString().endsWith("OptionalArg") }
        if (optional) return@map "[" + p.getSimpleName() + "]"
        "<" + p.getSimpleName() + ">"
    }.collect(Collectors.joining(" ")))
    found = true
}

override fun getSupportedSourceVersion(): SourceVersion? {
    return SourceVersion.latestSupported()
}
}
