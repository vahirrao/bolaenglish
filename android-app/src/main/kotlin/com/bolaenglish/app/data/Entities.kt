package com.bolaenglish.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

enum class NativeLanguage(val label: String) {
    HINDI("Hindi · हिन्दी"), MARATHI("Marathi · मराठी"), TAMIL("Tamil · தமிழ்"),
    TELUGU("Telugu · తెలుగు"), KANNADA("Kannada · ಕನ್ನಡ"), BENGALI("Bengali · বাংলা"),
    GUJARATI("Gujarati · ગુજરાતી"), ENGLISH("English")
}

enum class ProficiencyLevel(val label: String) {
    BEGINNER("Beginner"), ELEMENTARY("Elementary"), INTERMEDIATE("Intermediate")
}

@Entity
data class LearnerProfile(
    @PrimaryKey val id: Int = 1,
    val nativeLanguage: NativeLanguage,
    val proficiencyLevel: ProficiencyLevel,
    val dailyGoalMinutes: Int,
    val currentStreak: Int = 0,
    val totalXp: Int = 0,
    val onboardingCompleted: Boolean = false
)

@Entity
data class LessonProgress(
    @PrimaryKey val lessonId: String,
    val title: String,
    val completedStepsMask: Int = 0,
    val isCompleted: Boolean = false,
    val accuracyPercent: Int = 0,
    val lastPracticedAt: Long? = null
)

@Entity(indices = [Index("nextReviewAt")])
data class SpacedRepetitionItem(
    @PrimaryKey val id: String,
    val lessonId: String,
    val patternTitle: String,
    val promptText: String,
    val targetSentence: String,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 1,
    val repetitionCount: Int = 0,
    val nextReviewAt: Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
) {
    fun reviewed(recalled: Boolean, now: Long): SpacedRepetitionItem {
        // From introduction, on-time reviews fall on days 1, 2, 4, 7, 14 and 30.
        // After day 30, maintain monthly reviews. A lapse restarts the sequence.
        val count = if (recalled) repetitionCount + 1 else 0
        val gap = reviewGaps[count.coerceAtMost(reviewGaps.lastIndex)]
        return copy(
            repetitionCount = count, intervalDays = gap,
            nextReviewAt = now + TimeUnit.DAYS.toMillis(gap.toLong())
        )
    }

    private companion object {
        val reviewGaps = listOf(1, 1, 2, 3, 7, 16, 30)
    }
}

@Entity
data class DailyPracticeLog(
    @PrimaryKey val dateEpochDay: Long,
    val minutesPracticed: Int = 0,
    val xpEarned: Int = 0,
    val lessonsCompleted: Int = 0
)
