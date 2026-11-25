package fr.localspellcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LocalSpellcheckApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalSpellcheckApp() {
    val engine = remember { LanguageToolEngine() }
    var text by remember { mutableStateOf("Bonjour, je suis content de vous rencontré.\n\nCeci est un texte de teste.") }
    var language by remember { mutableStateOf("fr") }
    var corrections by remember { mutableStateOf<List<Correction>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    MaterialTheme(colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme())
        darkColorScheme() else lightColorScheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Correcteur local", fontWeight = FontWeight.SemiBold)
                            Text("LanguageTool · hors ligne", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        IconButton(onClick = { text = ""; corrections = emptyList(); error = null }) {
                            Icon(Icons.Default.Delete, contentDescription = "Effacer")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = language == "fr", onClick = { language = "fr"; corrections = emptyList() }, label = { Text("Français") })
                    FilterChip(selected = language == "en-US", onClick = { language = "en-US"; corrections = emptyList() }, label = { Text("English") })
                }

                Card(Modifier.fillMaxWidth().weight(1f)) {
                    Column(Modifier.fillMaxSize().padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Votre texte", style = MaterialTheme.typography.titleMedium)
                            Text("${text.length} caractères", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(12.dp))
                        BasicTextField(
                            value = text,
                            onValueChange = { text = it; corrections = emptyList() },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            decorationBox = { inner ->
                                Box(Modifier.fillMaxSize()) {
                                    if (text.isEmpty()) Text("Saisissez ou collez votre texte…",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    inner()
                                }
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            busy = true
                            error = null
                            try {
                                corrections = withContext(Dispatchers.IO) { engine.check(text, language) }
                                snackbar.showSnackbar(
                                    if (corrections.isEmpty()) "Aucune erreur détectée" else "${corrections.size} problème(s) détecté(s)"
                                )
                            } catch (t: Throwable) {
                                error = t.message ?: "Erreur pendant la correction"
                            } finally { busy = false }
                        }
                    },
                    enabled = text.isNotBlank() && !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (busy) "Analyse en cours…" else "Vérifier le texte")
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                if (corrections.isNotEmpty()) {
                    Text("${corrections.size} suggestions", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(
                        modifier = Modifier.weight(0.8f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(corrections) { item ->
                            Card {
                                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(item.message, style = MaterialTheme.typography.bodyMedium)
                                    Text("Règle : ${item.ruleId}", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    item.replacements.take(4).forEach { suggestion ->
                                        TextButton(onClick = {
                                            if (item.start in 0..text.length && item.end in item.start..text.length) {
                                                text = text.replaceRange(item.start, item.end, suggestion)
                                                corrections = emptyList()
                                            }
                                        }) { Text("Remplacer par « $suggestion »") }
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    "Traitement local : le texte n'est envoyé à aucun serveur par cette application.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
