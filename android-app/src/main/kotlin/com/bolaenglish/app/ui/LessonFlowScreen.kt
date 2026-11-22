package com.bolaenglish.app.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.bolaenglish.app.data.*
import com.bolaenglish.app.data.Module

// Preserve foreground practice across pauses and rotation. Each saved action starts a new timer.
@Composable
private fun practiceTime(): () -> Long {
    var accumulated by rememberSaveable { mutableLongStateOf(0) }
    var start by remember { mutableLongStateOf(0) }
    LifecycleResumeEffect(Unit) {
        start = SystemClock.elapsedRealtime()
        onPauseOrDispose {
            accumulated += SystemClock.elapsedRealtime() - start
            start = 0
        }
    }
    return { accumulated + if (start == 0L) 0 else SystemClock.elapsedRealtime() - start }
}

@Composable
fun LessonFlowScreen(module: Module, state: LessonState, model: LessonViewModel, back: () -> Unit) {
    val language = state.profile?.nativeLanguage ?: return
    val progress = state.progress[module.id]
    val step = (progress?.completedStepsMask ?: 0).countOneBits() + 1
    val due = state.due.filter { it.lessonId == module.id }
    key(module.id, step) {
        val elapsed = practiceTime()
        val finish = { firstTry: Boolean -> model.finish(module, step, firstTry, elapsed()) }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = back) { Text("Lesson hub") }
            Text(module.title, style = MaterialTheme.typography.headlineMedium)
            if (step <= 5) Text("Step $step of 5")
            when (step) {
                1 -> {
                    Text(module.pattern, style = MaterialTheme.typography.titleLarge)
                    Text(module.rule.forLanguage(language))
                    Text(module.example)
                    Text(module.meaning.forLanguage(language))
                    Button(onClick = { finish(true) }) { Text("Practice the pattern") }
                }
                2 -> Builder(module, finish)
                3 -> Translation(module, language, finish)
                4 -> SpeakingCard(module.speaking.forLanguage(language)) { finish(true) }
                5 -> {
                    Text("Recall practice", style = MaterialTheme.typography.titleLarge)
                    if (due.isEmpty()) {
                        // The review card earns its own time when rated.
                        val finishTime = practiceTime()
                        Text("You’re up to date. Your saved cards return when due.")
                        Button(onClick = { model.finish(module, step, true, finishTime()) }) { Text("Finish lesson") }
                    } else ReviewCard(due.first(), model, language)
                }
                else -> {
                    Text("Lesson complete · ${progress?.accuracyPercent}% first-try accuracy")
                    Text(module.pattern)
                    ListeningDrill(module.example)
                    if (due.isNotEmpty()) ReviewCard(due.first(), model, language)
                    Button(onClick = back) { Text("Return to lessons") }
                }
            }
        }
    }
}

@Composable
private fun Builder(module: Module, finish: (Boolean) -> Unit) {
    val words = remember(module) { module.example.split(" ") }
    val order = remember(module) {
        val indices = words.indices.toList()
        indices.shuffled(kotlin.random.Random(module.id.hashCode())).let {
            if (it == indices) indices.drop(1) + indices.first() else it
        }
    }
    var picked by rememberSaveable { mutableStateOf(intArrayOf()) }
    var missed by rememberSaveable { mutableStateOf(false) }
    val sentence = picked.joinToString(" ") { words[it] }
    val correct = sentence == module.example
    Text("Build the sentence", style = MaterialTheme.typography.titleLarge)
    Text(sentence.ifBlank { "Tap words in order." })
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        order.forEach { index ->
            OutlinedButton(enabled = index !in picked, onClick = {
                val next = picked + index
                picked = next
                if (next.size == words.size && next.joinToString(" ") { words[it] } != module.example) missed = true
            }) { Text(words[index]) }
        }
    }
    if (picked.isNotEmpty()) TextButton(onClick = { picked = intArrayOf() }) { Text("Start again") }
    if (picked.size == words.size && !correct) Text("Try again. Follow the pattern: ${module.pattern}")
    ListeningDrill(module.example)
    Button(enabled = correct, onClick = { finish(!missed) }) { Text("Continue to translation") }
}

@Composable
private fun Translation(module: Module, language: NativeLanguage, finish: (Boolean) -> Unit) {
    var answer by rememberSaveable { mutableStateOf("") }
    var checked by rememberSaveable { mutableStateOf(false) }
    var missed by rememberSaveable { mutableStateOf(false) }
    val correct = checked && module.accepts(answer)
    Text("Say it in English", style = MaterialTheme.typography.titleLarge)
    Text(module.question.forLanguage(language))
    OutlinedTextField(answer, { answer = it; checked = false }, label = { Text("Your answer") })
    if (checked) Text(if (correct) "Correct!" else module.tip.forLanguage(language))
    if (checked && !correct) Text("Model answer: ${module.target}")
    if (correct) Button(onClick = { finish(!missed) }) { Text("Continue to speaking") }
    else Button(enabled = answer.isNotBlank(), onClick = {
        checked = true
        if (!module.accepts(answer)) missed = true
    }) { Text("Check answer") }
}

@Composable
private fun ReviewCard(item: SpacedRepetitionItem, model: LessonViewModel, language: NativeLanguage) {
    key(item.id, item.nextReviewAt) {
        val elapsed = practiceTime()
        var revealed by rememberSaveable { mutableStateOf(false) }
        val module = model.modules.find { it.id == item.lessonId }
        Text(item.patternTitle)
        Text(if (item.id == module?.id + "-translation") module?.question?.forLanguage(language) ?: item.promptText else item.promptText)
        if (revealed) {
            Text(item.targetSentence, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { model.rate(item, false, elapsed()) }) { Text("Again") }
                Button(onClick = { model.rate(item, true, elapsed()) }) { Text("Recalled") }
            }
        } else Button(onClick = { revealed = true }) { Text("Reveal answer") }
    }
}

@Composable
fun ReviewScreen(due: List<SpacedRepetitionItem>, model: LessonViewModel, language: NativeLanguage, back: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = back) { Text("Lesson hub") }
        Text("Review · ${due.size} due", style = MaterialTheme.typography.headlineMedium)
        if (due.isEmpty()) Text("All caught up.") else ReviewCard(due.first(), model, language)
    }
}
