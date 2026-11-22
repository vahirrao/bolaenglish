package com.bolaenglish.app.ui.onboarding

import android.app.Application
import android.database.sqlite.SQLiteException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bolaenglish.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface LaunchState {
    data object Loading : LaunchState
    data object Onboarding : LaunchState
    data object Lessons : LaunchState
}

enum class SaveState { READY, SAVING, FAILED }

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LearnerRepository(LearnerDatabase.get(application))
    val launchState: StateFlow<LaunchState> = repository.profile
        .map { if (it?.onboardingCompleted == true) LaunchState.Lessons else LaunchState.Onboarding }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LaunchState.Loading)

    private val mutableSaveState = MutableStateFlow(SaveState.READY)
    val saveState = mutableSaveState.asStateFlow()

    fun finish(language: NativeLanguage, level: ProficiencyLevel, goal: Int) {
        if (mutableSaveState.value == SaveState.SAVING) return
        mutableSaveState.value = SaveState.SAVING
        viewModelScope.launch {
            try {
                repository.finishOnboarding(language, level, goal)
                mutableSaveState.value = SaveState.READY
            } catch (error: SQLiteException) {
                mutableSaveState.value = SaveState.FAILED
            }
        }
    }
}
