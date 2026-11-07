package buttondevteam.buttonproc

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate

class ButtonProcessor(val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        processConfigs(resolver)
        processSubcommands(resolver)

        return emptyList()
    }

    @OptIn(KspExperimental::class)
    private fun processConfigs(resolver: Resolver) {
        val configProcessor = ConfigProcessor(codeGenerator)
        val superClasses = resolver
            .getSymbolsWithAnnotation("buttondevteam.buttonproc.HasConfig") // TODO: Move annotation out of the annotation processor
            .filter { it.validate() }
            .filterIsInstance<KSClassDeclaration>()

        resolver.getNewFiles().flatMap { it.declarations.filterIsInstance<KSClassDeclaration>() }
            .map { cl -> Pair(cl, superClasses.firstOrNull { it in cl.superTypes.map { it.resolve().declaration } }) }
            .filter { it.second != null }
            .forEach { configProcessor.process(it.first, it.second!!.getAnnotationsByType(HasConfig::class).first()) }

        configProcessor.write()
    }

    private fun processSubcommands(resolver: Resolver) {
        val commandProcessor = CommandProcessor(codeGenerator)
        resolver.getSymbolsWithAnnotation("buttondevteam.lib.chat.Command2.Subcommand")
            .filter { it.validate() }
            .filterIsInstance<KSFunctionDeclaration>()
            .forEach { commandProcessor.process(it) }

        commandProcessor.write()
    }
}