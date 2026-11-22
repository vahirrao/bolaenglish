package com.bolaenglish.app.tutor

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bolaenglish.app.data.LearnerProfile

@Composable
fun TutorSettings(vm: VoiceTutorViewModel, back: () -> Unit) {
    var endpoint by remember { mutableStateOf(vm.connection?.endpoint.orEmpty()) }
    var key by remember { mutableStateOf(vm.connection?.key.orEmpty()) }
    var model by remember { mutableStateOf(vm.connection?.model.orEmpty()) }
    val settings = TutorConnection(endpoint.trim(), key.trim(), model.trim())
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = back) { Text("Back") }
        Text("Tutor settings", style = MaterialTheme.typography.headlineMedium)
        Text("Live mode sends your sentence, recent turns, language and level to your chosen chat completions endpoint. Settings last for this app session.")
        OutlinedTextField(endpoint, { endpoint = it }, label = { Text("Full HTTPS chat completions URL") }, singleLine = true)
        OutlinedTextField(model, { model = it }, label = { Text("Model ID") }, singleLine = true)
        OutlinedTextField(key, { key = it }, label = { Text("API key") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Button(enabled = settings.valid(), onClick = { vm.configure(settings); back() }) { Text("Use live tutor") }
        OutlinedButton(onClick = { vm.configure(null); back() }) { Text("Use offline tutor") }
    }
}

@Composable
fun VoiceTutorScreen(vm: VoiceTutorViewModel, profile: LearnerProfile, settings: () -> Unit, back: () -> Unit) {
    val context = LocalContext.current
    val pipeline = remember(context) { VoicePipelineManager(context) }
    val voice by pipeline.state.collectAsStateWithLifecycle()
    val chat by vm.state.collectAsStateWithLifecycle()
    var locale by rememberSaveable { mutableStateOf("en-IN") }
    LaunchedEffect(locale) { pipeline.setLocale(locale) }
    DisposableEffect(pipeline) { onDispose { pipeline.close() } }
    LifecycleResumeEffect(pipeline) {
        vm.resume()
        onPauseOrDispose { pipeline.pause(); vm.pause() }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pipeline.start() else pipeline.permissionDenied()
    }
    LaunchedEffect(voice.finalText) {
        if (voice.finalText.isNotBlank()) { vm.edit(voice.finalText); pipeline.consumed() }
    }
    val list = rememberLazyListState()
    var awaiting by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(chat.pending, chat.turns.size, chat.scenario) {
        if (awaiting && !chat.pending) {
            awaiting = false
            if (chat.error.isEmpty()) chat.turns.lastOrNull()?.feedback?.let { pipeline.speak(it.reply + " " + it.question) }
            list.animateScrollToItem(chat.turns.size + 1)
        }
    }

    LazyColumn(state = list, modifier = Modifier.fillMaxSize().imePadding(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row { TextButton(onClick = back) { Text("Lesson hub") }; TextButton(onClick = settings, enabled = !chat.pending) { Text("Tutor settings") } }
            Text("Voice tutor", style = MaterialTheme.typography.headlineMedium)
            Text(if (chat.configured) "Live tutor enabled" else "Offline tutor")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Scenario.entries.forEach { choice ->
                    FilterChip(selected = chat.scenario == choice, enabled = !chat.pending, onClick = {
                        if (choice != chat.scenario) { pipeline.pause(); pipeline.consumed(); vm.select(choice) }
                    }, label = { Text(choice.title) })
                }
            }
            Text(chat.scenario.opening)
        }
        items(chat.turns) { turn ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("You · " + turn.sentence)
                    Text(if (turn.live) "Live tutor" else "Offline tutor", style = MaterialTheme.typography.labelMedium)
                    Text(turn.feedback.reply)
                    Text("Practice sentence", style = MaterialTheme.typography.titleSmall)
                    Text(turn.feedback.suggestion)
                    Text(turn.feedback.tip)
                    Text(turn.feedback.question)
                    Row {
                        TextButton(enabled = voice.canSpeak, onClick = { pipeline.speak(turn.feedback.reply + " " + turn.feedback.question) }) { Text("Play reply") }
                        TextButton(enabled = voice.canSpeak, onClick = { pipeline.speak(turn.feedback.suggestion) }) { Text("Play sample") }
                    }
                }
            }
        }
        item {
            val capturing = voice.listening || voice.finishing
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (voice.playing != null) OutlinedButton(onClick = pipeline::pause) { Text("Stop playback") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("en-IN" to "Indian English", "en-US" to "US English").forEach { (tag, label) ->
                        FilterChip(selected = locale == tag, enabled = !capturing, onClick = { locale = tag }, label = { Text(label) })
                    }
                }
                if (voice.available) OutlinedButton(enabled = !chat.pending && !voice.finishing, onClick = {
                    if (voice.listening) pipeline.finishListening()
                    else if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) pipeline.start()
                    else permission.launch(Manifest.permission.RECORD_AUDIO)
                }) { Text(if (voice.listening) "Finish recording" else if (voice.finishing) "Transcribing…" else "Tap to speak") }
                else Text("Speech recognition unavailable. Type your sentence below.")
                if (voice.interim.isNotEmpty()) Text(voice.interim)
                if (voice.notice.isNotEmpty()) Text(voice.notice)
                OutlinedTextField(chat.draft, vm::edit, modifier = Modifier.fillMaxWidth(), enabled = !chat.pending && !capturing,
                    label = { Text("Your sentence") })
                if (chat.error.isNotEmpty()) Text(chat.error, color = MaterialTheme.colorScheme.error)
                Button(enabled = !chat.pending && !capturing && chat.draft.isNotBlank(), onClick = {
                    pipeline.pause(); awaiting = true; vm.send(profile)
                }) { Text(if (chat.pending) "Thinking…" else "Send") }
            }
        }
    }
}
