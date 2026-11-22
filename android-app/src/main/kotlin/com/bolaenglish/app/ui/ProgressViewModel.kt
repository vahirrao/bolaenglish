package com.bolaenglish.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bolaenglish.app.data.DailyPracticeLog
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt

data class ProgressSummary(
    val minutes: Int, val goal: Int, val streak: Int, val xp: Int,
    val completedLessons: Int, val averageAccuracy: Int?, val dueCount: Int,
    val history: List<DailyPracticeLog>
)

internal fun LessonState.summary(): ProgressSummary? {
    val learner = profile ?: return null
    val finished = progress.values.filter { it.isCompleted }
    val accuracy = if (finished.isEmpty()) null else finished.map { it.accuracyPercent }.average().roundToInt()
    return ProgressSummary(today?.minutesPracticed ?: 0, learner.dailyGoalMinutes, streak, learner.totalXp,
        finished.size, accuracy, due.size, recentHistory)
}

class ProgressViewModel(source: StateFlow<LessonState?>) : ViewModel() {
    val state = source.map { it?.summary() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), source.value?.summary())
}
