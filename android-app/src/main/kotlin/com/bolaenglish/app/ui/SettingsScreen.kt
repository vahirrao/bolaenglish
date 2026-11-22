package com.bolaenglish.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bolaenglish.app.data.*

@Composable
fun SettingsScreen(profile: LearnerProfile, vm: SettingsViewModel, configureTutor: () -> Unit, back: () -> Unit) {
    var language by rememberSaveable(profile.nativeLanguage) { mutableStateOf(profile.nativeLanguage) }
    var level by rememberSaveable(profile.proficiencyLevel) { mutableStateOf(profile.proficiencyLevel) }
    var goal by rememberSaveable(profile.dailyGoalMinutes) { mutableIntStateOf(profile.dailyGoalMinutes) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    val busy by vm.busy.collectAsStateWithLifecycle()
    val failed by vm.failed.collectAsStateWithLifecycle()
    val changed = language != profile.nativeLanguage || level != profile.proficiencyLevel || goal != profile.dailyGoalMinutes

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = back) { Text("Lesson hub") }
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        SettingChoices("Native language", NativeLanguage.entries, language, { it.label }, !busy) { language = it }
        SettingChoices("English level", ProficiencyLevel.entries, level, { it.label }, !busy) { level = it }
        SettingChoices("Daily practice goal", LearnerRepository.dailyGoals, goal, { "$it min" }, !busy) { goal = it }
        Button(enabled = changed && !busy, onClick = { vm.save(language, level, goal) }) {
            Text(if (busy) "Saving…" else "Save preferences")
        }
        if (failed) Text("Couldn’t update your profile. Please try again.", color = MaterialTheme.colorScheme.error)
        HorizontalDivider()
        OutlinedButton(onClick = configureTutor) { Text("Configure voice tutor") }
        TextButton(enabled = !busy, onClick = { confirm = true }) { Text("Replay onboarding") }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text("Replay onboarding?") },
        text = { Text("Choose your language, level and goal again. Your lessons, XP and practice history are kept.") },
        confirmButton = { TextButton(onClick = { confirm = false; vm.replay() }) { Text("Replay") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } })
}

@Composable
private fun <T> SettingChoices(title: String, options: List<T>, selected: T, label: (T) -> String, enabled: Boolean, select: (T) -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { option ->
            FilterChip(selected = selected == option, enabled = enabled, onClick = { select(option) }, label = { Text(label(option)) })
        }
    }
}
