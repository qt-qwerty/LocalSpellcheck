package fr.localspellcheck

import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
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
        private const val XERCES_DOCUMENT_BUILDER_FACTORY =
            "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl"
        private const val XERCES_SAX_PARSER_FACTORY =
            "org.apache.xerces.jaxp.SAXParserFactoryImpl"
        private const val XERCES_SCHEMA_FACTORY =
            "org.apache.xerces.jaxp.validation.XMLSchemaFactory"

        @Volatile
        private var xmlProviderReady = false

        /**
         * Android's built-in DocumentBuilderFactory/SAXParserFactory may throw
         * UnsupportedOperationException from setXIncludeAware(). LanguageTool
         * dependencies configure these factories, so select Xerces for all
         * three JAXP factory types before LanguageTool is initialized.
         */
        @Synchronized
        private fun ensureXmlProvider() {
            if (xmlProviderReady) return

            System.setProperty(
                "javax.xml.parsers.DocumentBuilderFactory",
                XERCES_DOCUMENT_BUILDER_FACTORY
            )
            System.setProperty(
                "javax.xml.parsers.SAXParserFactory",
                XERCES_SAX_PARSER_FACTORY
            )
            System.setProperty(
                "javax.xml.validation.SchemaFactory:${XMLConstants.W3C_XML_SCHEMA_NS_URI}",
                XERCES_SCHEMA_FACTORY
            )

            val classLoader = LanguageToolEngine::class.java.classLoader

            val documentFactory = DocumentBuilderFactory.newInstance(
                XERCES_DOCUMENT_BUILDER_FACTORY, classLoader
            )
            // Verify the operation that fails with Android's platform provider.
            documentFactory.isXIncludeAware = false
            documentFactory.isNamespaceAware = true
            documentFactory.newDocumentBuilder()

            val saxFactory = SAXParserFactory.newInstance(
                XERCES_SAX_PARSER_FACTORY, classLoader
            )
            saxFactory.isXIncludeAware = false
            saxFactory.isNamespaceAware = true
            saxFactory.newSAXParser()

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
