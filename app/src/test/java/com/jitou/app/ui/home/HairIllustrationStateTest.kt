package com.jitou.app.ui.home

import androidx.compose.ui.graphics.Color
import com.jitou.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class HairIllustrationStateTest {
    @Test
    fun noRecordUsesFreshIllustrationAndPlaceholderDays() {
        val state = hairIllustrationState(daysSinceLast = null, status = "清爽得很")

        assertEquals(R.drawable.hair_state_001, state.imageRes)
        assertEquals("先记一剪", state.bubbleText)
        assertEquals("--", state.daysText)
    }

    @Test
    fun freshStatusesUseFreshIllustration() {
        listOf("先记一剪", "清爽得很").forEach { status ->
            val state = hairIllustrationState(daysSinceLast = 6, status = status)

            assertEquals(R.drawable.hair_state_001, state.imageRes)
            assertEquals(status, state.bubbleText)
            assertEquals("6", state.daysText)
        }
    }

    @Test
    fun holdingStatusUsesGrowingIllustration() {
        val state = hairIllustrationState(daysSinceLast = 12, status = "还撑得住")

        assertEquals(R.drawable.hair_state_002, state.imageRes)
        assertEquals("还撑得住", state.bubbleText)
        assertEquals("12", state.daysText)
    }

    @Test
    fun almostDueStatusUsesAlmostDueIllustration() {
        val state = hairIllustrationState(daysSinceLast = 18, status = "差不多该约了，几时头")

        assertEquals(R.drawable.hair_state_003, state.imageRes)
        assertEquals("差不多该约了，几时头", state.bubbleText)
        assertEquals("18", state.daysText)
    }

    @Test
    fun dueStatusUsesLongIllustration() {
        val state = hairIllustrationState(daysSinceLast = 24, status = "是时候头了")

        assertEquals(R.drawable.hair_state_004, state.imageRes)
        assertEquals("是时候头了", state.bubbleText)
        assertEquals("24", state.daysText)
    }

    @Test
    fun illustrationUsesInkOnPaperColorsForTransparentPngs() {
        assertEquals(Color(0xFFFFFEFB), IllustrationPaper)
        assertEquals(Color(0xFF1C1A17), IllustrationInk)
    }
}
