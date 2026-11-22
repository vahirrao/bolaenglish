package com.bolaenglish.app.data

import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class SpacedRepetitionItemTest {
    @Test
    fun onTimeReviewsFollowMilestonesThenRepeatMonthly() {
        val day = TimeUnit.DAYS.toMillis(1)
        var item = SpacedRepetitionItem("one", "lesson", "Greeting", "Say hello", "Hello!", nextReviewAt = day)
        for (expectedDay in listOf(1, 2, 4, 7, 14, 30, 60)) {
            assertEquals(expectedDay * day, item.nextReviewAt)
            item = item.reviewed(recalled = true, now = item.nextReviewAt)
        }
        val lateReview = 100 * day
        val reset = item.reviewed(recalled = false, now = lateReview)
        assertEquals(0, reset.repetitionCount)
        assertEquals(1, reset.intervalDays)
        assertEquals(lateReview + day, reset.nextReviewAt)
    }
}
