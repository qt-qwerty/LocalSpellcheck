package fr.localspellcheck

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
    // JLanguageTool n'est pas thread-safe : une instance est créée par vérification.
    fun check(text: String, languageCode: String): List<Correction> {
        if (text.isBlank()) return emptyList()
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
