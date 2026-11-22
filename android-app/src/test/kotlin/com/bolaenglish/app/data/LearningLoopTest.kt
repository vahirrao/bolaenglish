package com.bolaenglish.app.data

import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone

class LearningLoopTest {
    @Test fun allModulesHaveDistinctIdsAndUsableBridges() {
        assertTrue(curriculum.size >= 5)
        assertEquals(curriculum.size, curriculum.map { it.id }.distinct().size)
        for (module in curriculum) {
            for (bridge in listOf(module.rule, module.meaning, module.question, module.tip, module.speaking)) {
                for (language in NativeLanguage.entries) assertTrue(bridge.forLanguage(language).isNotBlank())
                assertNotEquals(bridge.hindi, bridge.marathi)
                assertEquals(bridge.english, bridge.forLanguage(NativeLanguage.TAMIL))
            }
            assertTrue(module.accepts("  ${module.target.uppercase()}!  "))
            assertFalse(module.accepts("an unrelated sentence"))
        }
    }

    @Test fun progressCannotSkipOrRepeatAwards() {
        var progress = LessonProgress("routine", "Daily routines")
        assertNull(progress.advance(3, true, 1))
        for (step in 1..5) {
            progress = requireNotNull(progress.advance(step, step != 2, step.toLong()))
            assertNull(progress.advance(step, true, 99))
            assertEquals(step, progress.completedStepsMask.countOneBits())
            assertEquals(step == 5, progress.isCompleted)
        }
        assertEquals(50, progress.accuracyPercent)
        assertEquals(5L, progress.lastPracticedAt)
    }

    @Test fun localPracticeDayChangesAtIndianMidnight() {
        val zone = TimeZone.getTimeZone("Asia/Kolkata")
        val midnight = 18L * 3600000 + 30 * 60000
        assertEquals(0L, localEpochDay(midnight - 1, zone))
        assertEquals(1L, localEpochDay(midnight, zone))
    }
}
