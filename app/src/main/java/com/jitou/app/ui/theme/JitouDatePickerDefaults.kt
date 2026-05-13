package com.jitou.app.ui.theme

import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DatePickerInk = Color(0xFF171717)
private val DatePickerMutedInk = Color(0xFF72706A)
private val DatePickerYellow = Color(0xFFFFD84D)
private val DatePickerWarmPanel = Color(0xFFF0ECE2)
private val DatePickerSoftLine = Color(0x14000000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun jitouDatePickerColors(): DatePickerColors = DatePickerDefaults.colors(
    containerColor = Color.White,
    titleContentColor = DatePickerInk,
    headlineContentColor = DatePickerInk,
    weekdayContentColor = DatePickerMutedInk,
    subheadContentColor = DatePickerInk,
    navigationContentColor = DatePickerInk,
    yearContentColor = DatePickerInk,
    disabledYearContentColor = DatePickerMutedInk.copy(alpha = 0.38f),
    currentYearContentColor = DatePickerInk,
    selectedYearContentColor = DatePickerInk,
    disabledSelectedYearContentColor = DatePickerMutedInk.copy(alpha = 0.38f),
    selectedYearContainerColor = DatePickerYellow,
    disabledSelectedYearContainerColor = DatePickerWarmPanel.copy(alpha = 0.6f),
    dayContentColor = DatePickerInk,
    disabledDayContentColor = DatePickerMutedInk.copy(alpha = 0.38f),
    selectedDayContentColor = DatePickerInk,
    disabledSelectedDayContentColor = DatePickerMutedInk.copy(alpha = 0.38f),
    selectedDayContainerColor = DatePickerYellow,
    disabledSelectedDayContainerColor = DatePickerWarmPanel.copy(alpha = 0.6f),
    todayContentColor = DatePickerInk,
    todayDateBorderColor = DatePickerInk,
    dayInSelectionRangeContentColor = DatePickerInk,
    dayInSelectionRangeContainerColor = DatePickerWarmPanel,
    dividerColor = DatePickerSoftLine,
    dateTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DatePickerInk,
        unfocusedTextColor = DatePickerInk,
        disabledTextColor = DatePickerMutedInk.copy(alpha = 0.38f),
        errorTextColor = DatePickerInk,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = DatePickerWarmPanel,
        errorContainerColor = Color.White,
        cursorColor = DatePickerInk,
        errorCursorColor = DatePickerInk,
        focusedBorderColor = DatePickerInk,
        unfocusedBorderColor = DatePickerSoftLine,
        disabledBorderColor = DatePickerSoftLine,
        errorBorderColor = DatePickerInk,
        focusedLabelColor = DatePickerInk,
        unfocusedLabelColor = DatePickerMutedInk,
        disabledLabelColor = DatePickerMutedInk.copy(alpha = 0.38f),
        errorLabelColor = DatePickerInk,
    ),
)
