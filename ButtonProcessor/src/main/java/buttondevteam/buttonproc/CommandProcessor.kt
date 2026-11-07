package buttondevteam.buttonproc

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import org.bukkit.configuration.file.YamlConfiguration

class CommandProcessor(val codeGenerator: CodeGenerator) {
    private val yc = YamlConfiguration()

    fun process(method: KSFunctionDeclaration) {
        if (!method.annotations.any { it.shortName.getShortName() == "Subcommand" }) return
        //Need to do the 2 config sections at once so it doesn't overwrite the class section
        val cs = yc.createSection(method.qualifiedName?.asString() ?: throw Exception("Missing method qualified name"))
        println("Found subcommand: $method")
        cs.set("method", method.toString())
        cs.set("params", method.parameters.drop(1).joinToString(" ") { p ->
            if (p.annotations.any { am -> am.annotationType.resolve().declaration.simpleName.getShortName() == "OptionalArg" }) "[${p.name?.getShortName()}]"
            else "<${p.name?.getShortName()}>"
        })
    }

    fun write() {
        // Prevent double write to file
        if (yc.getKeys(false).isEmpty()) return

        val fo = codeGenerator.createNewFile(Dependencies.ALL_FILES, "", "commands", "yml")
        fo.write(yc.saveToString().toByteArray())
    }
}
