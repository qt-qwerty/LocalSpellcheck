package fr.localspellcheck

import javax.xml.XMLConstants
import javax.xml.validation.SchemaFactory
import org.languagetool.JLanguageTool
import org.languagetool.Languages
import org.languagetool.rules.RuleMatch

data class Correction(
    val start: Int,
    val end: Int,
    val message: String,
    val replacements: List<String>,
    val ruleId: String
)

class LanguageToolEngine {
    companion object {
        private const val XERCES_SCHEMA_FACTORY =
            "org.apache.xerces.jaxp.validation.XMLSchemaFactory"

        @Volatile
        private var xmlProviderReady = false

        /**
         * Force un provider JAXP embarqué au lieu de dépendre de la découverte
         * du provider XML du système Android. À exécuter avant toute initialisation LT.
         */
        @Synchronized
        private fun ensureXmlProvider() {
            if (xmlProviderReady) return

            val classLoader = LanguageToolEngine::class.java.classLoader
            System.setProperty(
                "javax.xml.validation.SchemaFactory:${XMLConstants.W3C_XML_SCHEMA_NS_URI}",
                XERCES_SCHEMA_FACTORY
            )

            // Échoue immédiatement avec un message explicite si le provider manque.
            SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI,
                XERCES_SCHEMA_FACTORY,
                classLoader
            )
            xmlProviderReady = true
        }
    }

    // Une instance JLanguageTool par vérification : elle n'est pas thread-safe.
    fun check(text: String, languageCode: String): List<Correction> {
        if (text.isBlank()) return emptyList()

        ensureXmlProvider()

        val language = Languages.getLanguageForShortCode(languageCode)
            ?: throw IllegalArgumentException("Langue non prise en charge : $languageCode")
        val tool = JLanguageTool(language)
        val matches: List<RuleMatch> = tool.check(text)

        return matches.map { match ->
            Correction(
                start = match.fromPos,
                end = match.toPos,
                message = match.message,
                replacements = match.suggestedReplacements,
                ruleId = match.rule.id
            )
        }
    }
}
