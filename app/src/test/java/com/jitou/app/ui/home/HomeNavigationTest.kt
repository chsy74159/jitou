package com.jitou.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeNavigationTest {
    @Test
    fun primaryScreensKeepPagerOrder() {
        assertEquals(
            listOf(JitouScreen.Home, JitouScreen.Appointment, JitouScreen.Profile),
            JitouScreen.primaryScreens(),
        )
        assertEquals(0, JitouScreen.Home.pageIndex)
        assertEquals(1, JitouScreen.Appointment.pageIndex)
        assertEquals(2, JitouScreen.Profile.pageIndex)
    }

    @Test
    fun systemBackReturnsSecondaryScreensToHome() {
        assertNull(JitouScreen.Home.systemBackTarget())
        assertEquals(JitouScreen.Home, JitouScreen.Appointment.systemBackTarget())
        assertEquals(JitouScreen.Home, JitouScreen.Profile.systemBackTarget())
    }
}
