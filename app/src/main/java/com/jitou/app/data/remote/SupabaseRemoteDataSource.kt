package com.jitou.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.time.Instant

class SupabaseRemoteDataSource(
    private val client: SupabaseClient,
) {
    fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    suspend fun fetchCurrentProfile(): RemoteProfile? {
        val currentUserId = currentUserId() ?: return null
        return client.from("profiles")
            .select {
                filter { eq("id", currentUserId) }
            }
            .decodeList<RemoteProfile>()
            .firstOrNull()
    }

    suspend fun fetchProfilesUpdatedSince(sinceMillis: Long): List<RemoteProfile> =
        client.from("profiles")
            .select {
                filter { gte("updated_at", sinceMillis.toRemoteTimestamp()) }
            }
            .decodeList()

    suspend fun fetchPairsUpdatedSince(sinceMillis: Long): List<RemoteHaircutPair> =
        client.from("haircut_pairs")
            .select {
                filter { gte("updated_at", sinceMillis.toRemoteTimestamp()) }
            }
            .decodeList()

    suspend fun fetchPairMembers(): List<RemoteHaircutPairMember> =
        client.from("haircut_pair_members")
            .select()
            .decodeList()

    suspend fun fetchHaircutRecordsUpdatedSince(sinceMillis: Long): List<RemoteHaircutRecord> =
        client.from("haircut_records")
            .select {
                filter { gte("updated_at", sinceMillis.toRemoteTimestamp()) }
            }
            .decodeList()

    suspend fun fetchJointPlansUpdatedSince(sinceMillis: Long): List<RemoteJointHaircutPlan> =
        client.from("joint_haircut_plans")
            .select {
                filter { gte("updated_at", sinceMillis.toRemoteTimestamp()) }
            }
            .decodeList()

    suspend fun fetchReminderPreferencesUpdatedSince(sinceMillis: Long): List<RemoteReminderPreference> =
        client.from("reminder_preferences")
            .select {
                filter { gte("updated_at", sinceMillis.toRemoteTimestamp()) }
            }
            .decodeList()

    suspend fun upsertHaircutRecord(record: RemoteHaircutRecord) {
        client.from("haircut_records").upsert(record) {
            onConflict = "id"
        }
    }

    suspend fun upsertCurrentProfileNickname(nickname: String): RemoteProfile? {
        val currentUserId = currentUserId() ?: return null
        val updatedAt = Instant.now().toString()
        client.from("profiles").upsert(
            remoteProfileNicknameUpdate(
                userId = currentUserId,
                nickname = nickname,
                updatedAt = updatedAt,
            ),
        ) {
            onConflict = "id"
        }
        client.from("haircut_pair_members").update(
            remotePairMemberDisplayNameUpdate(nickname),
        ) {
            filter { eq("user_id", currentUserId) }
        }
        return fetchCurrentProfile()
    }

    suspend fun upsertJointPlan(plan: RemoteJointHaircutPlan) {
        client.from("joint_haircut_plans").upsert(plan) {
            onConflict = "id"
        }
    }

    suspend fun confirmJointPlan(planId: String, currentUserId: String, updatedAt: String) {
        client.from("joint_haircut_plans").update(
            mapOf(
                "status" to "confirmed",
                "confirmed_by" to currentUserId,
                "confirmed_at" to updatedAt,
                "updated_at" to updatedAt,
            ),
        ) {
            filter { eq("id", planId) }
        }
    }

    suspend fun cancelJointPlan(planId: String, currentUserId: String, updatedAt: String) {
        client.from("joint_haircut_plans").update(
            mapOf(
                "status" to "cancelled",
                "cancelled_by" to currentUserId,
                "cancelled_at" to updatedAt,
                "updated_at" to updatedAt,
            ),
        ) {
            filter { eq("id", planId) }
        }
    }

    suspend fun completeJointPlan(planId: String, currentUserId: String, updatedAt: String) {
        client.from("joint_haircut_plans").update(
            mapOf(
                "status" to "completed",
                "completed_by" to currentUserId,
                "completed_at" to updatedAt,
                "updated_at" to updatedAt,
            ),
        ) {
            filter { eq("id", planId) }
        }
    }

    suspend fun upsertReminderPreference(preference: RemoteReminderPreference) {
        client.from("reminder_preferences").upsert(preference) {
            onConflict = "user_id"
        }
    }
}

fun Long.toRemoteTimestamp(): String = Instant.ofEpochMilli(this).toString()
