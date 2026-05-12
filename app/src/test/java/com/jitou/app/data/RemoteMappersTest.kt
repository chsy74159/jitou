package com.jitou.app.data

import com.jitou.app.data.remote.RemoteHaircutPairMember
import com.jitou.app.data.remote.RemoteJointHaircutPlan
import com.jitou.app.data.sync.parseRemoteTimestamp
import com.jitou.app.data.sync.planStatusForCurrentUser
import com.jitou.app.data.sync.toActiveProposalEntity
import com.jitou.app.model.ProposalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RemoteMappersTest {
    @Test
    fun pendingPlanFromCurrentUserMapsToPendingFriend() {
        assertEquals(
            ProposalStatus.PendingFriend,
            planStatusForCurrentUser(proposerId = CurrentUserId, currentUserId = CurrentUserId),
        )
    }

    @Test
    fun pendingPlanFromFriendMapsToPendingMe() {
        assertEquals(
            ProposalStatus.PendingMe,
            planStatusForCurrentUser(proposerId = FriendUserId, currentUserId = CurrentUserId),
        )
    }

    @Test
    fun remotePlanMapperKeepsFriendDisplayNameForIncomingProposal() {
        val entity = pendingPlan(proposerId = FriendUserId).toActiveProposalEntity(
            currentUserId = CurrentUserId,
            members = listOf(
                RemoteHaircutPairMember(PairId, CurrentUserId, "Sion", "2026-05-01T00:00:00Z"),
                RemoteHaircutPairMember(PairId, FriendUserId, "阿杰", "2026-05-01T00:00:00Z"),
            ),
        )

        assertEquals(ProposalStatus.PendingMe.name, entity.status)
        assertEquals("阿杰", entity.proposerName)
        assertEquals(910, entity.proposedMinuteOfDay)
    }

    @Test
    fun newerRemoteTimestampWinsConflictComparison() {
        val localUpdatedAt = Instant.parse("2026-05-10T10:00:00Z").toEpochMilli()
        val remoteUpdatedAt = parseRemoteTimestamp("2026-05-10T10:01:00Z") ?: 0L

        assertTrue(remoteUpdatedAt > localUpdatedAt)
    }

    private fun pendingPlan(proposerId: String) = RemoteJointHaircutPlan(
        id = "plan-1",
        pairId = PairId,
        proposerId = proposerId,
        proposedAt = "2026-05-10T15:10:00Z",
        status = "pending",
        reminderDaysBefore = 1,
        updatedAt = "2026-05-01T00:00:00Z",
    )

    private companion object {
        const val CurrentUserId = "user-sion"
        const val FriendUserId = "user-friend"
        const val PairId = "pair-1"
    }
}
