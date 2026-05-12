package com.jitou.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteProfile(
    val id: String,
    val nickname: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class RemoteHaircutPair(
    val id: String,
    val name: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class RemoteHaircutPairMember(
    @SerialName("pair_id") val pairId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("joined_at") val joinedAt: String,
)

@Serializable
data class RemoteHaircutRecord(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("haircut_date") val haircutDate: String,
    val note: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
data class RemoteJointHaircutPlan(
    val id: String,
    @SerialName("pair_id") val pairId: String,
    @SerialName("proposer_id") val proposerId: String,
    @SerialName("proposed_at") val proposedAt: String,
    val status: String,
    @SerialName("confirmed_by") val confirmedBy: String? = null,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("cancelled_by") val cancelledBy: String? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
    @SerialName("completed_by") val completedBy: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("reminder_days_before") val reminderDaysBefore: Int = 1,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class RemoteReminderPreference(
    @SerialName("user_id") val userId: String,
    val enabled: Boolean,
    @SerialName("days_before") val daysBefore: Int,
    @SerialName("reminder_time") val reminderTime: String,
    @SerialName("updated_at") val updatedAt: String,
)
