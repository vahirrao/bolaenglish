package com.bolaenglish.app.data

class LearnerRepository(private val db: LearnerDatabase) {
    val profile = db.profiles().observe()
    val lessons = db.lessons().observe()
    val practiceHistory = db.practice().observe()

    suspend fun finishOnboarding(language: NativeLanguage, level: ProficiencyLevel, goalMinutes: Int) {
        require(goalMinutes in dailyGoals)
        db.profiles().finish(language, level, goalMinutes)
    }

    suspend fun saveProgress(progress: LessonProgress) = db.lessons().save(progress)
    suspend fun savePractice(log: DailyPracticeLog) = db.practice().save(log)
    suspend fun addReviewItem(item: SpacedRepetitionItem) = db.reviews().add(item)
    suspend fun dueReviews(now: Long) = db.reviews().due(now)
    suspend fun recordReview(id: String, recalled: Boolean, now: Long) = db.reviews().review(id, recalled, now)

    companion object {
        val dailyGoals = listOf(15, 20, 30)
    }
}
