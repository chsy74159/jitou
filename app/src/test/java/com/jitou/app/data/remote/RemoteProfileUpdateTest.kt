package com.jitou.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteProfileUpdateTest {
    @Test
    fun nicknameUpdatePayloadsAlsoUpdatePairMemberDisplayName() {
        val profilePayload = remoteProfileNicknameUpdate(
            userId = "user-1",
            nickname = "新昵称",
            updatedAt = "2026-05-13T10:00:00Z",
        )

        assertEquals("user-1", profilePayload.id)
        assertEquals("新昵称", profilePayload.nickname)
        assertEquals("2026-05-13T10:00:00Z", profilePayload.updatedAt)
        assertEquals(
            mapOf("display_name" to "新昵称"),
            remotePairMemberDisplayNameUpdate("新昵称"),
        )
    }
}
