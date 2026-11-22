package com.bolaenglish.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bolaenglish.app.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LessonState(
    val profile: LearnerProfile?, val progress: Map<String, LessonProgress>,
    val today: DailyPracticeLog?, val streak: Int, val due: List<SpacedRepetitionItem>
)

class LessonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LessonRepository(LearnerDatabase.get(application))
    val modules = repository.modules

    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000)
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    private val reviews = clock.flatMapLatest { now ->
        repository.due(now).map { localEpochDay(now) to it }
    }

    val state = combine(repository.profile, repository.progress, repository.history, reviews) {
        profile, progress, logs, (day, due) ->
        val streak = if (logs.any { it.dateEpochDay in day - 1..day }) profile?.currentStreak ?: 0 else 0
        LessonState(profile, progress.associateBy { it.lessonId }, logs.find { it.dateEpochDay == day }, streak, due)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val failure = MutableStateFlow<String?>(null)
    val error = failure.asStateFlow()
    fun clearError() { failure.value = null }

    fun finish(module: Module, step: Int, firstTry: Boolean, elapsed: Long) =
        write { repository.finishStep(module, step, firstTry, elapsed) }

    fun rate(item: SpacedRepetitionItem, recalled: Boolean, elapsed: Long) =
        write { repository.rate(item, recalled, elapsed) }

    private fun write(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: android.database.SQLException) {
                failure.value = "Couldn’t save your practice. Please try again."
            }
        }
    }
}
