package com.jitou.app.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HaircutNotificationRulesTest {
    @Test
    fun schedulesThreeWeekPromptAtNineOnDay22() {
        val next = HaircutNotificationRules.nextNotificationAfter(
            records = recordsSince(LocalDate.of(2026, 5, 1)),
            proposalStatus = null,
            now = LocalDateTime.of(2026, 5, 23, 8, 30),
        )

        assertEquals(LocalDateTime.of(2026, 5, 23, 9, 0), next?.dateTime)
        assertEquals("已经头完三周了，准备几时头", next?.message)
    }

    @Test
    fun schedulesDay25To27PromptAtNine() {
        val next = HaircutNotificationRules.nextNotificationAfter(
            records = recordsSince(LocalDate.of(2026, 5, 1)),
            proposalStatus = null,
            now = LocalDateTime.of(2026, 5, 26, 8, 0),
        )

        assertEquals(LocalDateTime.of(2026, 5, 26, 9, 0), next?.dateTime)
        assertEquals("怎么说几头，差不多了了", next?.message)
    }

    @Test
    fun schedulesNextDay28To31PromptAfterPastSlot() {
        val next = HaircutNotificationRules.nextNotificationAfter(
            records = recordsSince(LocalDate.of(2026, 5, 1)),
            proposalStatus = null,
            now = LocalDateTime.of(2026, 5, 29, 9, 5),
        )

        assertEquals(LocalDateTime.of(2026, 5, 29, 12, 30), next?.dateTime)
        assertEquals("几时头，很关键", next?.message)
    }

    @Test
    fun schedulesDailyPromptsAfterDay31() {
        val next = HaircutNotificationRules.nextNotificationAfter(
            records = recordsSince(LocalDate.of(2026, 5, 1)),
            proposalStatus = null,
            now = LocalDateTime.of(2026, 6, 2, 17, 31),
        )

        assertEquals(LocalDateTime.of(2026, 6, 3, 8, 0), next?.dateTime)
        assertEquals("几头？？流星雨了都", next?.message)
    }

    @Test
    fun suppressesNotificationsForConfirmedOrWaitingFriendPlans() {
        val now = LocalDateTime.of(2026, 5, 29, 8, 0)
        val records = recordsSince(LocalDate.of(2026, 5, 1))

        assertNull(HaircutNotificationRules.nextNotificationAfter(records, ProposalStatus.Confirmed, now))
        assertNull(HaircutNotificationRules.nextNotificationAfter(records, ProposalStatus.PendingFriend, now))
    }

    @Test
    fun keepsNotificationsWhenFriendProposalNeedsCurrentUser() {
        val next = HaircutNotificationRules.nextNotificationAfter(
            records = recordsSince(LocalDate.of(2026, 5, 1)),
            proposalStatus = ProposalStatus.PendingMe,
            now = LocalDateTime.of(2026, 5, 29, 8, 0),
        )

        assertEquals(LocalDateTime.of(2026, 5, 29, 9, 0), next?.dateTime)
    }

    @Test
    fun returnsNotificationOnlyForExactScheduledMinute() {
        val records = recordsSince(LocalDate.of(2026, 5, 1))

        assertEquals(
            "今晚头。M集合",
            HaircutNotificationRules.notificationAt(
                records = records,
                proposalStatus = null,
                at = LocalDateTime.of(2026, 5, 29, 17, 30),
            )?.message,
        )
        assertNull(
            HaircutNotificationRules.notificationAt(
                records = records,
                proposalStatus = null,
                at = LocalDateTime.of(2026, 5, 29, 17, 31),
            ),
        )
    }

    private fun recordsSince(lastDate: LocalDate): List<HaircutRecord> = listOf(
        HaircutRecord(id = "latest", date = lastDate),
    )
}
