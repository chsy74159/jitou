package com.jitou.app.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitou.app.model.ReminderUiState
import com.jitou.app.ui.theme.jitouDatePickerColors
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

@Composable
internal fun QueueConfirmDialog(
    title: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = Ink,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("否", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Surface,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordHaircutDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    pastDatesOnly: Boolean = false,
) {
    val today = LocalDate.now()
    val initialDate = if (pastDatesOnly) today.minusDays(1) else today
    val selectableDates = remember(pastDatesOnly, today) {
        if (!pastDatesOnly) {
            DatePickerDefaults.AllDates
        } else {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis.toPickerDate().isBefore(today)

                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            }
        }
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toPickerMillis(),
        selectableDates = selectableDates,
    )
    val datePickerColors = jitouDatePickerColors()
    var pendingConfirmationDate by remember { mutableStateOf<LocalDate?>(null) }
    val selectedDate = pickerState.selectedDateMillis?.toPickerDate()
    val canSubmit = selectedDate != null && (!pastDatesOnly || selectedDate.isBefore(today))

    if (pendingConfirmationDate == null) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    enabled = canSubmit,
                    onClick = {
                        pendingConfirmationDate = selectedDate
                    },
                ) {
                    Text("记录")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            tonalElevation = 0.dp,
            colors = datePickerColors,
        ) {
            Column {
                DatePicker(state = pickerState, colors = datePickerColors)
                if (pastDatesOnly) {
                    Text(
                        text = "补录只能选择今天之前的日期",
                        color = MutedInk,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }

    pendingConfirmationDate?.let { date ->
        AlertDialog(
            onDismissRequest = { pendingConfirmationDate = null },
            title = {
                Text(
                    text = "确认补录头期？",
                    color = Ink,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                )
            },
            text = {
                Text(
                    text = "将补录 ${date.format(DateFormatter)} 这次剪头记录。",
                    color = MutedInk,
                    fontWeight = FontWeight.Bold,
                )
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(date) }) {
                    Text("确认提交", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingConfirmationDate = null }) {
                    Text("返回修改", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Surface,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderBottomSheet(
    reminder: ReminderUiState,
    onReminderChange: (ReminderUiState) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "提醒设置",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "当前只保存页面状态",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = reminder.enabled,
                    onCheckedChange = { onReminderChange(reminder.copy(enabled = it)) },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "提前 ${reminder.daysBefore} 天", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = reminder.daysBefore.toFloat(),
                    onValueChange = { onReminderChange(reminder.copy(daysBefore = it.roundToInt().coerceIn(1, 7))) },
                    valueRange = 1f..7f,
                    steps = 5,
                    enabled = reminder.enabled,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "提醒时间", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReminderTimeChip("早上", LocalTime.of(9, 0), reminder, onReminderChange)
                    ReminderTimeChip("晚上", LocalTime.of(20, 30), reminder, onReminderChange)
                }
            }

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("完成")
            }
        }
    }
}

@Composable
private fun ReminderTimeChip(
    label: String,
    time: LocalTime,
    reminder: ReminderUiState,
    onReminderChange: (ReminderUiState) -> Unit,
) {
    FilterChip(
        selected = reminder.time == time,
        enabled = reminder.enabled,
        onClick = { onReminderChange(reminder.copy(time = time)) },
        label = { Text("$label ${time.toReminderText()}") },
    )
}

@Composable
internal fun HaircutCheckinDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "打卡今日剪头",
                color = Ink,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
        },
        text = {
            Text(
                text = "长按下方按钮两秒完成打卡",
                color = MutedInk,
                fontWeight = FontWeight.Bold,
            )
        },
        confirmButton = {
            LongPressCheckinButton(onComplete = onConfirm)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Surface,
    )
}

@Composable
private fun LongPressCheckinButton(onComplete: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isComplete by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(WarmPanel, RoundedCornerShape(24.dp))
            .border(1.dp, SoftLine, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (isComplete) return@detectTapGestures
                        val job = scope.launch {
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
                            )
                            if (progress.value == 1f) {
                                isComplete = true
                                onComplete()
                            }
                        }
                        tryAwaitRelease()
                        job.cancel()
                        if (!isComplete) {
                            scope.launch {
                                progress.animateTo(0f, tween(300))
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.value)
                .fillMaxHeight()
                .background(Yellow)
                .align(Alignment.CenterStart)
        )
        Text(
            text = if (isComplete) "已完成！" else "长按剪头",
            color = Ink,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            letterSpacing = 0.sp
        )
    }
}
