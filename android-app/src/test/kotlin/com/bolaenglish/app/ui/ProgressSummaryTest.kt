package com.bolaenglish.app.ui

import com.bolaenglish.app.data.*
import org.junit.Assert.*
import org.junit.Test

class ProgressSummaryTest {
    private val profile = LearnerProfile(nativeLanguage = NativeLanguage.HINDI,
        proficiencyLevel = ProficiencyLevel.BEGINNER, dailyGoalMinutes = 20, totalXp = 84)
    private val empty = LessonState(profile, emptyMap(), null, 0, emptyList(), emptyList())

    @Test fun newLearnerShowsGoalAndNoAccuracyYet() {
        val result = requireNotNull(empty.summary())
        assertEquals(20, result.goal)
        assertEquals(0, result.minutes)
        assertEquals(0, result.completedLessons)
        assertNull(result.averageAccuracy)
    }

    @Test fun averageUsesCompletedLessonsIncludingZeroScores() {
        val log = DailyPracticeLog(90, 24, 12, 1)
        val lessons = listOf(LessonProgress("a", "A", isCompleted = true, accuracyPercent = 100),
            LessonProgress("b", "B", isCompleted = true, accuracyPercent = 0),
            LessonProgress("c", "C", accuracyPercent = 100))
        val due = SpacedRepetitionItem("card", "a", "Example", "Say hello", "Hello")
        val result = requireNotNull(empty.copy(today = log, streak = 3, recentHistory = listOf(log),
            progress = lessons.associateBy { it.lessonId }, due = listOf(due)).summary())
        assertEquals(50, result.averageAccuracy)
        assertEquals(2, result.completedLessons)
        assertEquals(24, result.minutes)
        assertEquals(3, result.streak)
        assertEquals(84, result.xp)
        assertEquals(1, result.dueCount)
        assertEquals(listOf(log), result.history)
    }
}
