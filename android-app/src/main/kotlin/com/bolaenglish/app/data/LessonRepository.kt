package com.bolaenglish.app.data

import androidx.room.withTransaction
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun localEpochDay(time: Long, zone: TimeZone = TimeZone.getDefault()): Long =
    Math.floorDiv(time + zone.getOffset(time), TimeUnit.DAYS.toMillis(1))

class LessonRepository(private val db: LearnerDatabase) {
    val modules = curriculum
    val profile = db.profiles().observe()
    val progress = db.lessons().observe()
    val history = db.practice().observe()
    fun due(now: Long) = db.reviews().observeDue(now)

    suspend fun finishStep(module: Module, step: Int, firstTry: Boolean, elapsed: Long) {
        require(step in 1..5)
        db.withTransaction {
            val now = System.currentTimeMillis()
            val old = db.lessons().find(module.id) ?: LessonProgress(module.id, module.title)
            val updated = old.advance(step, firstTry, now) ?: return@withTransaction
            if (step == 5 && db.reviews().due(now).any { it.lessonId == module.id }) return@withTransaction
            if (step == 3) {
                db.reviews().add(SpacedRepetitionItem(
                    id = module.id + "-translation", lessonId = module.id, patternTitle = module.pattern,
                    promptText = module.question.english, targetSentence = module.target,
                    nextReviewAt = now + TimeUnit.DAYS.toMillis(1)
                ))
            }
            db.lessons().save(updated)
            reward(now, elapsed, xp = if (step == 5) 20 else 10, completed = step == 5)
        }
    }

    suspend fun rate(item: SpacedRepetitionItem, recalled: Boolean, elapsed: Long) {
        db.withTransaction {
            val now = System.currentTimeMillis()
            val current = db.reviews().find(item.id) ?: return@withTransaction
            if (current.nextReviewAt != item.nextReviewAt || current.nextReviewAt > now) return@withTransaction
            db.reviews().update(current.reviewed(recalled, now))
            reward(now, elapsed, xp = if (recalled) 5 else 2, completed = false)
        }
    }

    private suspend fun reward(now: Long, elapsed: Long, xp: Int, completed: Boolean) {
        val day = localEpochDay(now)
        val profile = checkNotNull(db.profiles().current())
        val existing = db.practice().find(day)
        val log = existing ?: DailyPracticeLog(day)
        val millis = log.remainingMillis + elapsed.coerceAtLeast(0)
        db.practice().save(log.copy(minutesPracticed = log.minutesPracticed + (millis / 60000).toInt(),
            remainingMillis = millis % 60000, xpEarned = log.xpEarned + xp,
            lessonsCompleted = log.lessonsCompleted + if (completed) 1 else 0))
        val streak = when {
            existing != null -> profile.currentStreak.coerceAtLeast(1)
            db.practice().find(day - 1) != null -> profile.currentStreak + 1
            else -> 1
        }
        db.profiles().save(profile.copy(totalXp = profile.totalXp + xp, currentStreak = streak))
    }
}


internal fun LessonProgress.advance(step: Int, firstTry: Boolean, now: Long): LessonProgress? {
    require(step in 1..5)
    val priorMask = (1 shl (step - 1)) - 1
    if (completedStepsMask != priorMask) return null
    val score = if (firstTry) 100 else 0
    val accuracy = when (step) {
        2 -> score
        3 -> (accuracyPercent + score) / 2
        else -> accuracyPercent
    }
    return copy(completedStepsMask = priorMask or (1 shl (step - 1)), isCompleted = step == 5,
        accuracyPercent = accuracy, lastPracticedAt = now)
}
