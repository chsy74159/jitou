package com.jitou.app.ui.home

import androidx.compose.ui.graphics.Color
import com.jitou.app.R
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class HairIllustrationStateTest {
    @Test
    fun noRecordUsesFreshIllustrationAndPlaceholderDays() {
        val state = hairIllustrationState(daysSinceLast = null, status = "清爽得很")

        assertEquals(R.drawable.hair_state_001, state.imageRes)
        assertEquals(R.drawable.hair_state_001_dark, state.darkImageRes)
        assertEquals("先记一剪", state.bubbleText)
        assertEquals("--", state.daysText)
    }

    @Test
    fun freshStatusesUseFreshIllustration() {
        listOf("先记一剪", "清爽得很").forEach { status ->
            val state = hairIllustrationState(daysSinceLast = 6, status = status)

            assertEquals(R.drawable.hair_state_001, state.imageRes)
            assertEquals(R.drawable.hair_state_001_dark, state.darkImageRes)
            assertEquals(status, state.bubbleText)
            assertEquals("6", state.daysText)
        }
    }

    @Test
    fun holdingStatusUsesGrowingIllustration() {
        val state = hairIllustrationState(daysSinceLast = 12, status = "还撑得住")

        assertEquals(R.drawable.hair_state_002, state.imageRes)
        assertEquals(R.drawable.hair_state_002_dark, state.darkImageRes)
        assertEquals("还撑得住", state.bubbleText)
        assertEquals("12", state.daysText)
    }

    @Test
    fun almostDueStatusUsesAlmostDueIllustration() {
        val state = hairIllustrationState(daysSinceLast = 18, status = "差不多该约了，几时头")

        assertEquals(R.drawable.hair_state_003, state.imageRes)
        assertEquals(R.drawable.hair_state_003_dark, state.darkImageRes)
        assertEquals("差不多该约了，几时头", state.bubbleText)
        assertEquals("18", state.daysText)
    }

    @Test
    fun dueStatusUsesLongIllustration() {
        val state = hairIllustrationState(daysSinceLast = 24, status = "是时候头了")

        assertEquals(R.drawable.hair_state_004, state.imageRes)
        assertEquals(R.drawable.hair_state_004_dark, state.darkImageRes)
        assertEquals("是时候头了", state.bubbleText)
        assertEquals("24", state.daysText)
    }

    @Test
    fun illustrationUsesDarkResourceOnlyOnDarkBackground() {
        assertEquals(R.drawable.hair_state_002, hairIllustrationImageRes(R.drawable.hair_state_002, R.drawable.hair_state_002_dark, Color(0xFFF7F4EE)))
        assertEquals(R.drawable.hair_state_002_dark, hairIllustrationImageRes(R.drawable.hair_state_002, R.drawable.hair_state_002_dark, Color(0xFF15120F)))
    }

    @Test
    fun speechBubbleTailStaysNearLeftSideAcrossTextWidths() {
        assertEquals(0.24f, SpeechBubbleTailAnchorFraction)
        assertEquals(12f, SpeechBubbleTailDepthPx)
    }

    @Test
    fun todayLineUsesHomeDateFormat() {
        assertEquals("今天是2026.05.14", todayLineText(LocalDate.of(2026, 5, 14)))
    }
}
