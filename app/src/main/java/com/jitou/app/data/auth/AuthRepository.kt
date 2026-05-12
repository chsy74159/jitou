package com.jitou.app.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val client: SupabaseClient,
) {
    val sessionStatus: StateFlow<SessionStatus> = client.auth.sessionStatus

    suspend fun restoreSession() {
        client.auth.loadFromStorage()
        client.auth.awaitInitialization()
    }

    suspend fun signIn(account: String, password: String) {
        client.auth.signInWith(Email) {
            email = normalizeJitouEmail(account)
            this.password = password
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    companion object {
        const val InternalEmailDomain = "@jitou.app"

        fun normalizeJitouEmail(input: String): String {
            val trimmed = input.trim()
            return if ('@' in trimmed) trimmed else "$trimmed$InternalEmailDomain"
        }
    }
}
