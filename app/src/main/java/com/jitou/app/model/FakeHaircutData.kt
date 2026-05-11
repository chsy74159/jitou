package com.jitou.app.model

import java.time.LocalDate
import java.time.LocalTime

fun fakeHaircutRecords(today: LocalDate = LocalDate.now()): List<HaircutRecord> = listOf(
    HaircutRecord(id = "record-1", date = today.minusDays(241), note = "开始记录"),
    HaircutRecord(id = "record-2", date = today.minusDays(223)),
    HaircutRecord(id = "record-3", date = today.minusDays(177), note = "拖久了"),
    HaircutRecord(id = "record-4", date = today.minusDays(142)),
    HaircutRecord(id = "record-5", date = today.minusDays(115)),
    HaircutRecord(id = "record-6", date = today.minusDays(87)),
    HaircutRecord(id = "record-7", date = today.minusDays(52), note = "和朋友一起"),
    HaircutRecord(id = "record-8", date = today.minusDays(23), note = "清爽一点"),
)

fun fakeReminderState(): ReminderUiState = ReminderUiState(
    enabled = true,
    daysBefore = 2,
    time = LocalTime.of(20, 30),
)
