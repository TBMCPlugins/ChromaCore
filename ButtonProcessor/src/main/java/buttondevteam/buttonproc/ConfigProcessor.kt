package buttondevteam.buttonproc

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import org.bukkit.configuration.file.YamlConfiguration

class ConfigProcessor(val codeGenerator: CodeGenerator) {
    private val yc = YamlConfiguration()

    fun process(targetcl: KSClassDeclaration, hasConfig: HasConfig) {
        if (targetcl.modifiers.contains(Modifier.ABSTRACT)) return
        val path = if (hasConfig.global) "global" else "components." + targetcl.simpleName.getShortName()
        println(path)
        for (e in targetcl.getAllProperties()) {
            val propertyType = e.type.resolve()
            if (!propertyType.declaration.simpleName.getShortName().contains("ConfigData")) continue

            val doc = e.docString ?: continue
            println("Adding docs for config: ${e.simpleName.getShortName()}")
            yc.set("$path.${e.simpleName.getShortName()}", doc.trim { it <= ' ' })
        }
        val doc = targetcl.docString
        if (doc != null) {
            println("Adding docs for class: " + targetcl.simpleName.getShortName())
            yc.set("$path.generalDescriptionInsteadOfAConfig", doc.trim { it <= ' ' })
        }
    }

    fun write() {
        // Prevent double write to file
        if (yc.getKeys(false).isEmpty()) return

        val fo = codeGenerator.createNewFile(Dependencies.ALL_FILES, "", "configHelp", "yml")
        fo.write(yc.saveToString().toByteArray())
    }
}
