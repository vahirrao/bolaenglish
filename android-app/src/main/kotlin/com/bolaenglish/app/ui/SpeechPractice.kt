package com.bolaenglish.app.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
internal fun ListeningDrill(sentence: String) {
    val context = LocalContext.current.applicationContext
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }
    var ready by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        var active = true
        val tts = TextToSpeech(context) { status ->
            if (active && status == TextToSpeech.SUCCESS) {
                ready = (engine?.setLanguage(Locale.US) ?: TextToSpeech.LANG_NOT_SUPPORTED) >= 0
            }
        }
        engine = tts
        onDispose {
            active = false
            tts.stop()
            tts.shutdown()
        }
    }
    Text(sentence)
    if (ready) {
        OutlinedButton(onClick = {
            if (engine?.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "example") == TextToSpeech.ERROR) ready = false
        }) { Text("Listen and repeat") }
    } else {
        Text("Read the sentence aloud to practice pronunciation.")
    }
}

@Composable
internal fun SpeakingCard(prompt: String, finish: () -> Unit) {
    var transcript by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var checked by rememberSaveable { mutableStateOf(false) }
    val capture = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            transcript = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (transcript.isBlank()) message = "No words captured. Try again or self-check aloud."
        }
    }
    Card { Column(Modifier.padding(16.dp)) { Text(prompt) } }
    OutlinedButton(onClick = {
        message = ""
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        try { capture.launch(intent) }
        catch (_: ActivityNotFoundException) { message = "Speech capture unavailable. Practice aloud and self-check." }
        catch (_: SecurityException) { message = "Speech capture unavailable. Practice aloud and self-check." }
    }) { Text("Capture speech (optional)") }
    if (transcript.isNotBlank()) Text("Heard: $transcript")
    if (message.isNotBlank()) Text(message)
    FilterChip(selected = checked, onClick = { checked = !checked },
        label = { Text("I spoke aloud and checked the pattern") })
    Button(onClick = finish, enabled = checked) { Text("Complete speaking practice") }
}
