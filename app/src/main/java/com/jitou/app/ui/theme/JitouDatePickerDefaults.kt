package com.jitou.app.ui.theme

import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun jitouDatePickerColors(): DatePickerColors {
    val colors = MaterialTheme.jitouColors

    return DatePickerDefaults.colors(
        containerColor = colors.surface,
        titleContentColor = colors.ink,
        headlineContentColor = colors.ink,
        weekdayContentColor = colors.mutedInk,
        subheadContentColor = colors.ink,
        navigationContentColor = colors.ink,
        yearContentColor = colors.ink,
        disabledYearContentColor = colors.mutedInk.copy(alpha = 0.38f),
        currentYearContentColor = colors.accentStrong,
        selectedYearContentColor = colors.ink,
        disabledSelectedYearContentColor = colors.mutedInk.copy(alpha = 0.38f),
        selectedYearContainerColor = colors.accent,
        disabledSelectedYearContainerColor = colors.surfaceMuted.copy(alpha = 0.6f),
        dayContentColor = colors.ink,
        disabledDayContentColor = colors.mutedInk.copy(alpha = 0.38f),
        selectedDayContentColor = colors.ink,
        disabledSelectedDayContentColor = colors.mutedInk.copy(alpha = 0.38f),
        selectedDayContainerColor = colors.accent,
        disabledSelectedDayContainerColor = colors.surfaceMuted.copy(alpha = 0.6f),
        todayContentColor = colors.accentStrong,
        todayDateBorderColor = colors.accentStrong,
        dayInSelectionRangeContentColor = colors.ink,
        dayInSelectionRangeContainerColor = colors.surfaceMuted,
        dividerColor = colors.line,
        dateTextFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.ink,
            unfocusedTextColor = colors.ink,
            disabledTextColor = colors.mutedInk.copy(alpha = 0.38f),
            errorTextColor = colors.danger,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            disabledContainerColor = colors.surfaceMuted,
            errorContainerColor = colors.surface,
            cursorColor = colors.ink,
            errorCursorColor = colors.danger,
            focusedBorderColor = colors.accentStrong,
            unfocusedBorderColor = colors.line,
            disabledBorderColor = colors.line,
            errorBorderColor = colors.danger,
            focusedLabelColor = colors.ink,
            unfocusedLabelColor = colors.mutedInk,
            disabledLabelColor = colors.mutedInk.copy(alpha = 0.38f),
            errorLabelColor = colors.danger,
        ),
    )
}
