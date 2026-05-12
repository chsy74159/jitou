package com.jitou.app.data

import com.jitou.app.data.auth.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRepositoryTest {
    @Test
    fun normalizeJitouEmail_appendsInternalDomainForShortAccount() {
        assertEquals("sion@jitou.app", AuthRepository.normalizeJitouEmail("sion"))
    }

    @Test
    fun normalizeJitouEmail_keepsCompleteEmail() {
        assertEquals("sion@example.com", AuthRepository.normalizeJitouEmail("sion@example.com"))
    }

    @Test
    fun normalizeJitouEmail_trimsInput() {
        assertEquals("sion@jitou.app", AuthRepository.normalizeJitouEmail("  sion  "))
    }
}
