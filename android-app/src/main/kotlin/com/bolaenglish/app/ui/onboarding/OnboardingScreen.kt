package com.bolaenglish.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bolaenglish.app.data.LearnerRepository
import com.bolaenglish.app.data.NativeLanguage
import com.bolaenglish.app.data.ProficiencyLevel

@Composable
fun OnboardingScreen(saveState: SaveState, onFinish: (NativeLanguage, ProficiencyLevel, Int) -> Unit) {
    var language by rememberSaveable { mutableStateOf<NativeLanguage?>(null) }
    var level by rememberSaveable { mutableStateOf<ProficiencyLevel?>(null) }
    var goal by rememberSaveable { mutableIntStateOf(15) }
    var placement by rememberSaveable { mutableStateOf(false) }
    val enabled = saveState != SaveState.SAVING
    BackHandler(enabled = placement) { if (enabled) placement = false }

    key(placement) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!placement) {
                Text("Choose your native language", style = MaterialTheme.typography.headlineMedium)
                Column(Modifier.selectableGroup()) {
                    NativeLanguage.entries.forEach { option ->
                        val featured = option == NativeLanguage.HINDI || option == NativeLanguage.MARATHI
                        Surface(
                            color = if (featured) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Choice(option.label, option == language) { language = option }
                        }
                    }
                }
                Button(onClick = { placement = true }, enabled = language != null) { Text("Continue") }
            } else {
                Text("Choose your English level", style = MaterialTheme.typography.headlineMedium)
                Column(Modifier.selectableGroup()) {
                    ProficiencyLevel.entries.forEach { option ->
                        Choice(option.label, option == level, enabled) { level = option }
                    }
                }
                Text("Daily practice goal", style = MaterialTheme.typography.titleLarge)
                Column(Modifier.selectableGroup()) {
                    LearnerRepository.dailyGoals.forEach { minutes ->
                        Choice("$minutes minutes", minutes == goal, enabled) { goal = minutes }
                    }
                }
                if (saveState == SaveState.FAILED) {
                    Text("Couldn’t save your choices. Try again.", color = MaterialTheme.colorScheme.error)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { placement = false }, enabled = enabled) { Text("Back") }
                    Button(
                        onClick = { onFinish(requireNotNull(language), requireNotNull(level), goal) },
                        enabled = enabled && level != null
                    ) { Text(if (enabled) "Start learning" else "Saving…") }
                }
            }
        }
    }
}

@Composable
private fun Choice(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 48.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Text(label)
    }
}
