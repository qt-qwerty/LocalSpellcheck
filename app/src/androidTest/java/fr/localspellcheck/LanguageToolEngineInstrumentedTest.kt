package fr.localspellcheck

import androidx.test.ext.junit.runners.AndroidJUnit4
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import javax.xml.validation.SchemaFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageToolEngineInstrumentedTest {

    @Test
    fun allXmlFactoriesUseXercesAndSupportXIncludeConfiguration() {
        // Initializes and installs the JAXP providers before LanguageTool starts.
        LanguageToolEngine().check("A short test.", "en-US")

        val documentFactory = DocumentBuilderFactory.newInstance()
        assertTrue(
            "DocumentBuilderFactory must be Xerces, got ${documentFactory.javaClass.name}",
            documentFactory.javaClass.name == "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl"
        )
        documentFactory.isXIncludeAware = false
        documentFactory.newDocumentBuilder()

        val saxFactory = SAXParserFactory.newInstance()
        assertTrue(
            "SAXParserFactory must be Xerces, got ${saxFactory.javaClass.name}",
            saxFactory.javaClass.name == "org.apache.xerces.jaxp.SAXParserFactoryImpl"
        )
        saxFactory.isXIncludeAware = false
        saxFactory.newSAXParser()

        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        assertNotNull(schemaFactory)
        assertTrue(schemaFactory.isSchemaLanguageSupported(XMLConstants.W3C_XML_SCHEMA_NS_URI))
    }

    @Test
    fun frenchModuleChecksTextOffline() {
        val matches = LanguageToolEngine().check(
            "Bonjour, je suis content de vous rencontré.",
            "fr"
        )
        assertFalse("Le module français doit détecter une erreur grammaticale.", matches.isEmpty())
        assertFalse(matches.any { it.start < 0 || it.end < it.start })
    }

    @Test
    fun englishModuleChecksTextOffline() {
        val matches = LanguageToolEngine().check("I has a book.", "en-US")
        assertFalse("Le module anglais doit détecter une erreur grammaticale.", matches.isEmpty())
        assertFalse(matches.any { it.start < 0 || it.end < it.start })
    }
}
