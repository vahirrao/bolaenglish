package com.bolaenglish.app.tutor

import com.bolaenglish.app.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class TutorTest {
    private val learner = LearnerProfile(nativeLanguage = NativeLanguage.MARATHI,
        proficiencyLevel = ProficiencyLevel.BEGINNER, dailyGoalMinutes = 20)

    @Test fun offlineCorrectionKeepsMeaningAndLocalizesTip() {
        val input = "I didn't went to the market."
        val hi = OfflineTutor.reply(input, Scenario.DAILY, NativeLanguage.HINDI, ProficiencyLevel.BEGINNER, 0)
        val mr = OfflineTutor.reply(input, Scenario.DAILY, NativeLanguage.MARATHI, ProficiencyLevel.BEGINNER, 0)
        assertEquals("I didn't go to the market.", hi.suggestion)
        assertTrue(hi.tip.contains("के बाद"))
        assertTrue(mr.tip.contains("नंतर"))
    }

    @Test fun correctQuestionAndScenarioContextArePreserved() {
        val reply = OfflineTutor.reply("Does he go to the station?", Scenario.TRAVEL, NativeLanguage.TAMIL,
            ProficiencyLevel.INTERMEDIATE, 0)
        assertEquals("Does he go to the station?", reply.suggestion)
        assertTrue(reply.question.contains("ticket"))
        assertTrue(reply.tip.contains("reason"))
    }

    @Test fun liveContractIncludesProfileAndConversationAndFallsBackOnFailure() = runBlocking {
        var down = false
        val fake = object : TutorApi {
            override suspend fun chat(endpoint: String, auth: String, request: ChatRequest): ChatReply {
                if (down) throw IOException("offline")
                assertEquals("test-model", request.model)
                assertTrue(request.messages.first().content.contains("MARATHI"))
                assertTrue(request.messages.first().content.contains("BEGINNER"))
                assertTrue(request.messages.any { it.content == "Yesterday I studied." })
                return ChatReply(listOf(Choice(Message("assistant",
                    """{"reply":"Nice detail.","suggestion":"I study English.","tip":"वेळ सांगा.","question":"When do you study?"}"""))))
            }
        }
        val repo = VoiceTutorRepository(fake)
        val config = TutorConnection("https://example.invalid/v1/chat/completions", "unit-test", "test-model")
        val first = repo.respond("Yesterday I studied.", Scenario.DAILY, learner, emptyList(), null)
        assertFalse(first.live)
        assertTrue(repo.respond("I study English.", Scenario.DAILY, learner, listOf(first), config).live)
        down = true
        assertFalse(repo.respond("I study English.", Scenario.DAILY, learner, listOf(first), config).live)
    }

    @Test fun completedTutorTurnsAccumulateTimeAndXpWithoutCompletingLessons() {
        val start = DailyPracticeLog(7, minutesPracticed = 2, xpEarned = 20, lessonsCompleted = 1, remainingMillis = 45000)
        val twoTurns = start.recordActivity(30000, TUTOR_TURN_XP, false).recordActivity(20000, TUTOR_TURN_XP, false)
        assertEquals(3, twoTurns.minutesPracticed)
        assertEquals(35000L, twoTurns.remainingMillis)
        assertEquals(36, twoTurns.xpEarned)
        assertEquals(1, twoTurns.lessonsCompleted)
    }
}
