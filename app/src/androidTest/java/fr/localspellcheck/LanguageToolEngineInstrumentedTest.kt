package fr.localspellcheck

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import javax.xml.XMLConstants
import javax.xml.validation.SchemaFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageToolEngineInstrumentedTest {

    @Test
    fun xmlSchemaProviderIsAvailableOnAndroid() {
        val factory = SchemaFactory.newInstance(
            XMLConstants.W3C_XML_SCHEMA_NS_URI,
            "org.apache.xerces.jaxp.validation.XMLSchemaFactory",
            LanguageToolEngine::class.java.classLoader
        )
        assertNotNull(factory)
        assertFalse(factory.isSchemaLanguageSupported("unsupported:test-schema"))
    }

    @Test
    fun frenchModuleChecksTextOffline() {
        val matches = LanguageToolEngine().check(
            "Je suis content de vous rencontré.",
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
