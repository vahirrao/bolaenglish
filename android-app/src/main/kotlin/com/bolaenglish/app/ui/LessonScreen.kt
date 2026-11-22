package com.bolaenglish.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bolaenglish.app.data.Module

@Composable
fun LessonHub(state: LessonState, modules: List<Module>, open: (String) -> Unit, review: () -> Unit, tutor: () -> Unit) {
    val learner = state.profile ?: return
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Lesson hub", style = MaterialTheme.typography.headlineMedium)
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(learner.nativeLanguage.label, Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
        item {
            Text("Today · ${state.today?.minutesPracticed ?: 0} / ${learner.dailyGoalMinutes} min")
            Text("${learner.totalXp} XP  ·  ${state.streak}-day streak")
            OutlinedButton(onClick = review, enabled = state.due.isNotEmpty()) {
                Text("Review due cards (${state.due.size})")
            }
        }
        item { Button(onClick = tutor) { Text("Practice with the voice tutor") } }
        items(modules, key = { it.id }) { module ->
            val progress = state.progress[module.id]
            val steps = (progress?.completedStepsMask ?: 0).countOneBits()
            Card(onClick = { open(module.id) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(module.title, style = MaterialTheme.typography.titleLarge)
                    Text(module.pattern)
                    LinearProgressIndicator(progress = { steps / 5f }, modifier = Modifier.fillMaxWidth())
                    Text(if (steps == 5) "Completed · ${progress?.accuracyPercent}% first-try accuracy" else "$steps of 5 steps")
                }
            }
        }
    }
}
