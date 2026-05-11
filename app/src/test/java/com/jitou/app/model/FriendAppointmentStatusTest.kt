package com.jitou.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FriendAppointmentStatusTest {
    @Test
    fun labelMapsProposalStatusForFriendCard() {
        assertEquals("未发起约头", FriendAppointmentStatus.label(null))
        assertEquals("等待对方确认", FriendAppointmentStatus.label(ProposalStatus.PendingFriend))
        assertEquals("对方已发起", FriendAppointmentStatus.label(ProposalStatus.PendingMe))
        assertEquals("已经约头", FriendAppointmentStatus.label(ProposalStatus.Confirmed))
    }
}
