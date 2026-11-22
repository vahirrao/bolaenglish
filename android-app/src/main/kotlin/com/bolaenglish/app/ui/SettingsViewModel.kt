package com.bolaenglish.app.ui

import android.app.Application
import android.database.SQLException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bolaenglish.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val profiles = LearnerDatabase.get(app).profiles()
    private val writing = MutableStateFlow(false)
    val busy = writing.asStateFlow()
    private val failure = MutableStateFlow(false)
    val failed = failure.asStateFlow()

    fun save(language: NativeLanguage, level: ProficiencyLevel, goal: Int) = write {
        require(goal in LearnerRepository.dailyGoals)
        profiles.updatePreferences(language, level, goal)
    }

    fun replay() = write { profiles.replayOnboarding() }

    private fun write(action: suspend () -> Unit) {
        if (writing.value) return
        writing.value = true
        failure.value = false
        viewModelScope.launch {
            try { action() }
            catch (_: SQLException) { failure.value = true }
            finally { writing.value = false }
        }
    }
}
