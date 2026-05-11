package com.jitou.app.model

object FriendAppointmentStatus {
    fun label(proposalStatus: ProposalStatus?): String = when (proposalStatus) {
        null -> "未发起约头"
        ProposalStatus.PendingFriend -> "等待对方确认"
        ProposalStatus.PendingMe -> "对方已发起"
        ProposalStatus.Confirmed -> "已经约头"
    }
}
