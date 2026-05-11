package com.jitou.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueStateTest {
    @Test
    fun joinedQueueTurnsOnQueueing() {
        val next = QueueState.reduce(isQueueing = false, event = QueueEvent.JoinedQueue)

        assertTrue(next)
    }

    @Test
    fun cancelledQueueAndRecordedHaircutTurnOffQueueing() {
        assertFalse(QueueState.reduce(isQueueing = true, event = QueueEvent.CancelledQueue))
        assertFalse(QueueState.reduce(isQueueing = true, event = QueueEvent.RecordedHaircut))
    }

    @Test
    fun friendQueueNoticeOnlyAppearsForConfirmedProposalWhileQueueing() {
        assertTrue(
            QueueState.shouldShowFriendQueueNotice(
                isQueueing = true,
                proposalStatus = ProposalStatus.Confirmed,
            ),
        )
        assertFalse(
            QueueState.shouldShowFriendQueueNotice(
                isQueueing = true,
                proposalStatus = ProposalStatus.PendingFriend,
            ),
        )
        assertFalse(
            QueueState.shouldShowFriendQueueNotice(
                isQueueing = false,
                proposalStatus = ProposalStatus.Confirmed,
            ),
        )
    }

    @Test
    fun queueButtonTextReflectsState() {
        assertEquals("去排队", QueueState.buttonText(isQueueing = false))
        assertEquals("正在排队", QueueState.buttonText(isQueueing = true))
    }

    @Test
    fun queueNoticeTextDistinguishesOwnCardFromFriendCard() {
        assertEquals("正在排队", QueueState.queueNoticeText(isOwnCard = true))
        assertEquals("对方正在排队", QueueState.queueNoticeText(isOwnCard = false))
    }
}
