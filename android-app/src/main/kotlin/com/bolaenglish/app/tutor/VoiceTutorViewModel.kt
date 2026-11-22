package com.bolaenglish.app.tutor

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bolaenglish.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TutorState(val scenario: Scenario = Scenario.DAILY, val turns: List<Turn> = emptyList(),
    val draft: String = "", val pending: Boolean = false, val error: String = "", val configured: Boolean = false)

class VoiceTutorViewModel(app: Application) : AndroidViewModel(app) {
    private val tutor = VoiceTutorRepository()
    private val practice = LessonRepository(LearnerDatabase.get(app))
    private val mutable = MutableStateFlow(TutorState())
    val state = mutable.asStateFlow()
    private val conversations = mutableMapOf<Scenario, List<Turn>>()
    var connection: TutorConnection? = null
        private set
    private var activeSince: Long? = null
    private var elapsed = 0L
    private var foreground = false

    fun configure(value: TutorConnection?) {
        require(value == null || value.valid())
        connection = value
        mutable.update { it.copy(configured = value != null) }
    }
    fun resume() {
        foreground = true
        if (!mutable.value.pending && activeSince == null) activeSince = SystemClock.elapsedRealtime()
    }
    fun pause() {
        foreground = false
        stopClock()
    }
    private fun stopClock() {
        activeSince?.let { elapsed += SystemClock.elapsedRealtime() - it }
        activeSince = null
    }
    fun select(scenario: Scenario) {
        if (mutable.value.pending || scenario == mutable.value.scenario) return
        elapsed = 0
        activeSince = if (foreground) SystemClock.elapsedRealtime() else null
        mutable.update { it.copy(scenario = scenario, turns = conversations[scenario].orEmpty(), draft = "", error = "") }
    }
    fun edit(text: String) { mutable.update { it.copy(draft = text.take(1500), error = "") } }

    fun send(profile: LearnerProfile) {
        val before = mutable.value
        val sentence = before.draft.trim()
        if (before.pending || sentence.isEmpty()) return
        stopClock()
        mutable.update { it.copy(pending = true, error = "") }
        viewModelScope.launch {
            try {
                val turn = tutor.respond(sentence, before.scenario, profile, before.turns, connection)
                practice.completeTutorTurn(elapsed)
                elapsed = 0L
                val turns = before.turns + turn
                conversations[before.scenario] = turns
                mutable.update { it.copy(turns = turns, draft = "") }
            } catch (_: android.database.SQLException) {
                mutable.update { it.copy(error = "Couldn’t save this turn. Please send again.") }
            } finally {
                mutable.update { it.copy(pending = false) }
                if (foreground) resume()
            }
        }
    }
}
