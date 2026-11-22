package com.bolaenglish.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@Composable
fun ProgressScreen(model: ProgressViewModel, review: () -> Unit, back: () -> Unit) {
    val summary by model.state.collectAsStateWithLifecycle()
    val data = summary ?: return
    // Epoch days already represent local calendar dates. Format in UTC to avoid shifting the date twice.
    val dates = DateFormat.getDateInstance(DateFormat.MEDIUM).apply { timeZone = TimeZone.getTimeZone("UTC") }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            TextButton(onClick = back) { Text("Lesson hub") }
            Text("Progress", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Today’s practice", style = MaterialTheme.typography.titleMedium)
                    Text("${data.minutes} of ${data.goal} minutes")
                    LinearProgressIndicator(progress = { (data.minutes.toFloat() / data.goal.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item {
            Text("${data.streak}-day streak  ·  ${data.xp} total XP")
            Text("Completed lessons: ${data.completedLessons}")
            Text("Average first-try accuracy: " + (data.averageAccuracy?.let { "$it%" } ?: "—"))
            OutlinedButton(onClick = review, enabled = data.dueCount > 0) { Text("Review due cards (${data.dueCount})") }
        }
        item { Text("Recent practice", style = MaterialTheme.typography.titleLarge) }
        if (data.history.isEmpty()) item { Text("Your practice days will appear here after your first activity.") }
        items(data.history, key = { it.dateEpochDay }) { log ->
            ListItem(headlineContent = { Text(dates.format(Date(TimeUnit.DAYS.toMillis(log.dateEpochDay)))) },
                supportingContent = { Text("${log.minutesPracticed} min  ·  ${log.lessonsCompleted} lessons completed") },
                trailingContent = { Text("+${log.xpEarned} XP") })
        }
    }
}
