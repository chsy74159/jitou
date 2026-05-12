package com.jitou.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitou.app.model.ReminderUiState
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
        containerColor = Color.White,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordHaircutDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = LocalDate.now().toPickerMillis())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = pickerState.selectedDateMillis?.toPickerDate() ?: LocalDate.now()
                    onConfirm(selected)
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
    ) {
        DatePicker(state = pickerState)
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
