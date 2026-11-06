package buttondevteam.buttonproc

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import org.bukkit.configuration.file.YamlConfiguration
import java.io.IOException
import java.io.OutputStream
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

class ConfigProcessor(val codeGenerator: CodeGenerator) {
    private val yc = YamlConfiguration()
    private val fo: OutputStream = codeGenerator.createNewFile(Dependencies.ALL_FILES, "", "configHelp", "yml")

    @OptIn(KspExperimental::class)
    fun process(targetcl: KSClassDeclaration) {
        if (targetcl.modifiers.contains(Modifier.ABSTRACT)) return
        val hasConfig = targetcl.getAnnotationsByType(HasConfig::class).firstOrNull()
        if (hasConfig == null) {
            println("That's not our HasConfig annotation...")
            return
        }
        val path = if (hasConfig.global) "global" else "components." + targetcl.simpleName
        for (e in targetcl.getAllFunctions()) { // TODO: getAllProperties()
            val tm: TypeMirror
            if (e is ExecutableElement) tm = e.getReturnType()
            else if (e.getKind().isField()) tm = e.asType()
            else continue
            if (tm.getKind() != TypeKind.DECLARED) continue
            val dt = tm as DeclaredType
            if (!dt.asElement().getSimpleName().toString().contains("ConfigData")) continue  //Ahhha! There was a return here! (MinecraftChatModule getListener())


            val doc = procEnv.getElementUtils().getDocComment(e)
            if (doc == null) continue
            println("Adding docs for config: " + e.getSimpleName())
            yc.set(path + "." + e.getSimpleName(), doc.trim { it <= ' ' })
        }
        val javadoc = procEnv.getElementUtils().getDocComment(targetcl)
        if (javadoc != null) {
            println("Adding docs for class: " + targetcl.getSimpleName())
            yc.set(path + ".generalDescriptionInsteadOfAConfig", javadoc.trim { it <= ' ' })
        }
        try {
            yc.save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun write() {
        fo.write(yc.saveToString().toByteArray())
    }
}
