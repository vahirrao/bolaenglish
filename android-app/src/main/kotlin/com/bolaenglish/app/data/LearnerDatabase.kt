package com.bolaenglish.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProfileDao {
    @Query("SELECT * FROM LearnerProfile WHERE id = 1")
    abstract fun observe(): Flow<LearnerProfile?>

    @Query("SELECT * FROM LearnerProfile WHERE id = 1")
    abstract suspend fun current(): LearnerProfile?

    @Query("UPDATE LearnerProfile SET nativeLanguage = :language, proficiencyLevel = :level, dailyGoalMinutes = :goal WHERE id = 1")
    abstract suspend fun updatePreferences(language: NativeLanguage, level: ProficiencyLevel, goal: Int)

    @Query("UPDATE LearnerProfile SET onboardingCompleted = 0 WHERE id = 1")
    abstract suspend fun replayOnboarding()

    @Upsert
    abstract suspend fun save(profile: LearnerProfile)

    @Transaction
    open suspend fun finish(language: NativeLanguage, level: ProficiencyLevel, goal: Int) {
        val profile = current() ?: LearnerProfile(nativeLanguage = language, proficiencyLevel = level, dailyGoalMinutes = goal)
        save(profile.copy(nativeLanguage = language, proficiencyLevel = level, dailyGoalMinutes = goal, onboardingCompleted = true))
    }
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM LessonProgress WHERE lessonId = :id")
    suspend fun find(id: String): LessonProgress?

    @Upsert suspend fun save(progress: LessonProgress)
    @Query("SELECT * FROM LessonProgress ORDER BY lastPracticedAt DESC, lessonId")
    fun observe(): Flow<List<LessonProgress>>
}

@Dao
abstract class ReviewDao {
    @Query("SELECT * FROM SpacedRepetitionItem WHERE nextReviewAt <= :now ORDER BY nextReviewAt, id")
    abstract fun observeDue(now: Long): Flow<List<SpacedRepetitionItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun add(item: SpacedRepetitionItem)

    @Query("SELECT * FROM SpacedRepetitionItem WHERE nextReviewAt <= :now ORDER BY nextReviewAt, id")
    abstract suspend fun due(now: Long): List<SpacedRepetitionItem>

    @Query("SELECT * FROM SpacedRepetitionItem WHERE id = :id")
    abstract suspend fun find(id: String): SpacedRepetitionItem?

    @Update abstract suspend fun update(item: SpacedRepetitionItem)

    @Transaction
    open suspend fun review(id: String, recalled: Boolean, now: Long) {
        val item = find(id) ?: return
        if (item.nextReviewAt <= now) update(item.reviewed(recalled, now))
    }
}

@Dao
interface PracticeDao {
    @Query("SELECT * FROM DailyPracticeLog WHERE dateEpochDay = :day")
    suspend fun find(day: Long): DailyPracticeLog?

    @Upsert suspend fun save(log: DailyPracticeLog)
    @Query("SELECT * FROM DailyPracticeLog ORDER BY dateEpochDay DESC")
    fun observe(): Flow<List<DailyPracticeLog>>
}

@Database(
    entities = [LearnerProfile::class, LessonProgress::class, SpacedRepetitionItem::class, DailyPracticeLog::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class LearnerDatabase : RoomDatabase() {
    abstract fun profiles(): ProfileDao
    abstract fun lessons(): LessonDao
    abstract fun reviews(): ReviewDao
    abstract fun practice(): PracticeDao

    companion object {
        @Volatile private var instance: LearnerDatabase? = null

        fun get(context: Context): LearnerDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, LearnerDatabase::class.java, "learner.db")
                .build().also { instance = it }
        }
    }
}
