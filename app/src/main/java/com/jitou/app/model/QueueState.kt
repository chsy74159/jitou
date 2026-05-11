package com.jitou.app.model

enum class QueueEvent {
    JoinedQueue,
    CancelledQueue,
    RecordedHaircut,
}

object QueueState {
    fun reduce(isQueueing: Boolean, event: QueueEvent): Boolean = when (event) {
        QueueEvent.JoinedQueue -> true
        QueueEvent.CancelledQueue,
        QueueEvent.RecordedHaircut -> false
    }

    fun shouldShowFriendQueueNotice(
        isQueueing: Boolean,
        proposalStatus: ProposalStatus?,
    ): Boolean = isQueueing && proposalStatus == ProposalStatus.Confirmed

    fun buttonText(isQueueing: Boolean): String = if (isQueueing) "正在排队" else "去排队"

    fun queueNoticeText(isOwnCard: Boolean): String = if (isOwnCard) "正在排队" else "对方正在排队"
}
