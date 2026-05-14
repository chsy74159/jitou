package com.jitou.app.data

import com.jitou.app.data.remote.RemoteHaircutPairMember
import com.jitou.app.data.remote.RemoteJointHaircutPlan
import com.jitou.app.data.sync.completedJointPlan
import com.jitou.app.data.sync.jointPlanStatusForSyncState
import com.jitou.app.data.sync.parseRemoteTimestamp
import com.jitou.app.data.sync.planStatusForCurrentUser
import com.jitou.app.data.sync.toActiveProposalEntity
import com.jitou.app.data.local.SyncState
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

    @Test
    fun pendingCompleteSyncStateMapsToCompletedRemoteStatus() {
        assertEquals(
            "completed",
            jointPlanStatusForSyncState(
                syncState = SyncState.PENDING_COMPLETE,
                localStatus = ProposalStatus.Confirmed.name,
            ),
        )
    }

    @Test
    fun completedJointPlanCarriesCompletedActorAndTimestamp() {
        val plan = completedJointPlan(
            id = "plan-2",
            pairId = PairId,
            proposerId = CurrentUserId,
            proposedAt = "2026-05-10T15:10:00Z",
            reminderDaysBefore = 1,
            completedBy = CurrentUserId,
            completedAt = "2026-05-14T10:00:00Z",
        )

        assertEquals("completed", plan.status)
        assertEquals(CurrentUserId, plan.completedBy)
        assertEquals("2026-05-14T10:00:00Z", plan.completedAt)
        assertEquals("2026-05-14T10:00:00Z", plan.updatedAt)
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
