package com.jitou.app.ui.appointment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitou.app.R
import com.jitou.app.model.AppointmentHistoryItem
import com.jitou.app.model.FriendAppointmentStatus
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.ProposalStatus
import com.jitou.app.ui.theme.JitouTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val AppointmentDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val AppointmentTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val PageBackground = Color(0xFFF8F7F2)
private val InkColor = Color(0xFF171717)
private val AccentYellow = Color(0xFFFFD84D)
private val MutedInk = Color(0xFF72706A)
private val WarmPanel = Color(0xFFF0ECE2)
private val SoftLine = Color(0x14000000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentRoute(
    proposal: HaircutProposal?,
    historyItems: List<AppointmentHistoryItem>,
    averageIntervalDays: Int,
    friendDaysSinceLast: Int,
    friendName: String,
    isFriendQueueing: Boolean,
    onBack: () -> Unit,
    onProposalChange: (HaircutProposal?) -> Unit,
    onHistoryAdd: (AppointmentHistoryItem) -> Unit,
    onCompleteHaircut: (LocalDate) -> Unit,
) {
    var flowStep by remember { mutableStateOf(AppointmentFlowStep.Date) }
    var selectedDate by remember { mutableStateOf(recommendedDate(averageIntervalDays)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(20, 30)) }
    var showStartFlow by remember { mutableStateOf(false) }
    var editingProposal by remember { mutableStateOf<HaircutProposal?>(null) }

    Scaffold(contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppointmentTopBar()

            SectionTitle("当前共同计划")
            CurrentPlanCard(
                proposal = proposal,
                showStartFlow = showStartFlow,
                step = flowStep,
                selectedDate = selectedDate,
                selectedTime = selectedTime,
                averageIntervalDays = averageIntervalDays,
                friendName = friendName,
                onStart = {
                    flowStep = AppointmentFlowStep.Date
                    selectedDate = recommendedDate(averageIntervalDays)
                    selectedTime = LocalTime.of(20, 30)
                    editingProposal = null
                    showStartFlow = true
                },
                onDateChange = { selectedDate = it },
                onTimeChange = { selectedTime = it },
                onStepChange = { flowStep = it },
                onSend = {
                    val editedProposal = editingProposal
                    val nextProposal = editedProposal?.copy(
                        proposedDate = selectedDate,
                        proposedTime = selectedTime,
                        proposerName = "我",
                        status = ProposalStatus.PendingFriend,
                    ) ?:
                        HaircutProposal(
                            id = "proposal-${System.currentTimeMillis()}",
                            proposedDate = selectedDate,
                            proposedTime = selectedTime,
                            proposerName = "我",
                            status = ProposalStatus.PendingFriend,
                        )
                    onProposalChange(nextProposal)
                    flowStep = AppointmentFlowStep.Date
                    editingProposal = null
                    showStartFlow = false
                },
                onEditDate = {
                    proposal?.let {
                        selectedDate = it.proposedDate
                        selectedTime = it.proposedTime
                        editingProposal = it
                        showStartFlow = true
                    }
                    flowStep = AppointmentFlowStep.Date
                },
                onCancel = {
                    showStartFlow = false
                    editingProposal = null
                    onProposalChange(null)
                },
                onAgree = {
                    proposal?.let {
                        onProposalChange(it.copy(status = ProposalStatus.Confirmed, reminderDaysBefore = 1))
                    }
                },
                onReject = {
                    showStartFlow = false
                    editingProposal = null
                    onProposalChange(null)
                },
                onComplete = {
                    proposal?.let {
                        onHistoryAdd(
                            AppointmentHistoryItem(
                                id = "history-${System.currentTimeMillis()}",
                                date = it.proposedDate,
                                time = it.proposedTime,
                                companionName = friendName,
                                result = "已完成",
                            ),
                        )
                        onCompleteHaircut(it.proposedDate)
                    }
                },
            )

            SectionTitle("头友状态")
            FriendStatusCard(
                friendName = friendName,
                daysSinceLast = friendDaysSinceLast,
                status = FriendAppointmentStatus.label(proposal?.status),
                isQueueing = isFriendQueueing,
            )

            SectionTitle("历史约剪")
            HistoryList(items = historyItems)
            Spacer(modifier = Modifier.height(104.dp))
        }
    }
}

@Composable
private fun AppointmentTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "约剪",
            modifier = Modifier.weight(1f),
            color = InkColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
        Text(
            text = "CO-OP",
            color = InkColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            modifier = Modifier
                .background(AccentYellow, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = InkColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.sp,
    )
}

@Composable
private fun CurrentPlanCard(
    proposal: HaircutProposal?,
    showStartFlow: Boolean,
    step: AppointmentFlowStep,
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    averageIntervalDays: Int,
    friendName: String,
    onStart: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onStepChange: (AppointmentFlowStep) -> Unit,
    onSend: () -> Unit,
    onEditDate: () -> Unit,
    onCancel: () -> Unit,
    onAgree: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
) {
    if (showStartFlow) {
        StartAppointmentFlow(
            step = step,
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            averageIntervalDays = averageIntervalDays,
            friendName = friendName,
            onDateChange = onDateChange,
            onTimeChange = onTimeChange,
            onStepChange = onStepChange,
            onSend = onSend,
        )
    } else {
        FramedPanel {
            when (proposal?.status) {
                null -> NoPlanState(onStart = onStart)
                ProposalStatus.PendingFriend -> WaitingFriendState(
                    proposal = proposal,
                    friendName = friendName,
                    onEditDate = onEditDate,
                    onCancel = onCancel,
                )
                ProposalStatus.PendingMe -> WaitingMeState(
                    proposal = proposal,
                    friendName = friendName,
                    onAgree = onAgree,
                    onEditDate = onEditDate,
                    onReject = onReject,
                )
                ProposalStatus.Confirmed -> ConfirmedState(
                    proposal = proposal,
                    friendName = friendName,
                    onEditDate = onEditDate,
                    onComplete = onComplete,
                    onCancel = onCancel,
                )
            }
        }
    }
}

@Composable
private fun NoPlanState(onStart: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("暂无约剪计划", color = InkColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text("可以邀请朋友一起定下一次剪头日期", color = MutedInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        YellowActionButton(text = "发起约头", icon = Icons.Rounded.ContentCut, onClick = onStart)
    }
}

@Composable
private fun WaitingFriendState(
    proposal: HaircutProposal,
    friendName: String,
    onEditDate: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("等待${friendName}确认", color = InkColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        DetailBlock(label = "你提议：", value = proposal.dateTimeText())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WhiteActionButton("修改日期", onEditDate, Modifier.weight(1f))
            WhiteActionButton("取消", onCancel, Modifier.weight(1f))
        }
    }
}

@Composable
private fun WaitingMeState(
    proposal: HaircutProposal,
    friendName: String,
    onAgree: () -> Unit,
    onEditDate: () -> Unit,
    onReject: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("${friendName}已发起", color = InkColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(proposal.dateTimeText(), color = InkColor, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            YellowActionButton("接受", Icons.Rounded.Check, onAgree, Modifier.weight(1f))
            WhiteActionButton("修改日期", onEditDate, Modifier.weight(1f))
            WhiteActionButton("拒绝", onReject, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ConfirmedState(
    proposal: HaircutProposal,
    friendName: String,
    onEditDate: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("约日程已建立", color = InkColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(proposal.dateTimeText(), color = InkColor, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text("同行：$friendName", color = MutedInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("提醒：提前 ${proposal.reminderDaysBefore} 天", color = MutedInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WhiteActionButton("改期", onEditDate, Modifier.weight(1f))
            YellowActionButton("完成剪头", Icons.Rounded.Check, onComplete, Modifier.weight(1.25f))
            WhiteActionButton("取消计划", onCancel, Modifier.weight(1.25f))
        }
    }
}

@Composable
private fun FriendStatusCard(
    friendName: String,
    daysSinceLast: Int,
    status: String,
    isQueueing: Boolean,
) {
    FramedPanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(AccentYellow, CircleShape)
                    .border(2.dp, InkColor, CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cartoon_avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(friendName, color = InkColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(
                        text = status,
                        color = InkColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .background(AccentYellow, RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                Text(
                    text = "距离上次头 $daysSinceLast 天",
                    color = MutedInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (isQueueing) {
                    Text(
                        text = "正在排队",
                        color = InkColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .background(WarmPanel, RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartAppointmentFlow(
    step: AppointmentFlowStep,
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    averageIntervalDays: Int,
    friendName: String,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onStepChange: (AppointmentFlowStep) -> Unit,
    onSend: () -> Unit,
) {
    FramedPanel {
        StepIndicator(step)
        when (step) {
            AppointmentFlowStep.Date -> DateStep(
                selectedDate = selectedDate,
                averageIntervalDays = averageIntervalDays,
                onDateChange = onDateChange,
                onNext = { onStepChange(AppointmentFlowStep.Time) },
            )
            AppointmentFlowStep.Time -> TimeStep(
                selectedTime = selectedTime,
                onTimeChange = onTimeChange,
                onBack = { onStepChange(AppointmentFlowStep.Date) },
                onNext = { onStepChange(AppointmentFlowStep.Send) },
            )
            AppointmentFlowStep.Send -> SendStep(
                selectedDate = selectedDate,
                selectedTime = selectedTime,
                friendName = friendName,
                onBack = { onStepChange(AppointmentFlowStep.Time) },
                onSend = onSend,
            )
        }
    }
}

@Composable
private fun StepIndicator(step: AppointmentFlowStep) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AppointmentFlowStep.entries.forEach { item ->
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .weight(1f)
                    .background(if (item == step) AccentYellow else WarmPanel, RoundedCornerShape(12.dp))
                    .border(1.dp, SoftLine, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.label, color = InkColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateStep(
    selectedDate: LocalDate,
    averageIntervalDays: Int,
    onDateChange: (LocalDate) -> Unit,
    onNext: () -> Unit,
) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toPickerMillis())
    LaunchedEffect(pickerState.selectedDateMillis) {
        pickerState.selectedDateMillis?.let { onDateChange(it.toPickerDate()) }
    }
    val cycleDate = recommendedDate(averageIntervalDays)
    val weekendDate = nextWeekendDate()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("初定剪头日期", color = InkColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SoftLine, RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp)),
        ) {
            DatePicker(state = pickerState, title = null, headline = null)
        }
        Text("推荐日期：", color = InkColor, fontSize = 13.sp, fontWeight = FontWeight.Black)
        RecommendationRow("${cycleDate.format(AppointmentDateFormatter)}，按你的平均周期") { onDateChange(cycleDate) }
        RecommendationRow("${weekendDate.format(AppointmentDateFormatter)}，周末更方便") { onDateChange(weekendDate) }
        YellowActionButton("下一步", Icons.Rounded.EditCalendar, onNext)
    }
}

@Composable
private fun TimeStep(
    selectedTime: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("时间", color = InkColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text("自定义时间：${selectedTime.format(AppointmentTimeFormatter)}", color = InkColor, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeChoice("15:00", LocalTime.of(15, 0), selectedTime, onTimeChange, Modifier.weight(1f))
            TimeChoice("20:30", LocalTime.of(20, 30), selectedTime, onTimeChange, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WhiteActionButton("-30分钟", { onTimeChange(selectedTime.minusMinutes(30)) }, Modifier.weight(1f))
            WhiteActionButton("+30分钟", { onTimeChange(selectedTime.plusMinutes(30)) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WhiteActionButton("上一步", onBack, Modifier.weight(1f))
            YellowActionButton("下一步", Icons.Rounded.Check, onNext, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SendStep(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    friendName: String,
    onBack: () -> Unit,
    onSend: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("发送给${friendName}确认？", color = InkColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        DetailBlock(label = "你提议：", value = "${selectedDate.dateWithWeekday()} ${selectedTime.format(AppointmentTimeFormatter)}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WhiteActionButton("上一步", onBack, Modifier.weight(1f))
            YellowActionButton("发送", Icons.AutoMirrored.Rounded.Send, onSend, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RecommendationRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarmPanel, RoundedCornerShape(18.dp))
            .border(1.dp, SoftLine, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = InkColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp)
    }
}

@Composable
private fun TimeChoice(
    label: String,
    time: LocalTime,
    selectedTime: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { onTimeChange(time) },
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (time == selectedTime) AccentYellow else WarmPanel,
            contentColor = InkColor,
        ),
        border = BorderStroke(1.dp, SoftLine),
        elevation = null,
    ) {
        Text(label, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
    }
}

@Composable
private fun HistoryList(items: List<AppointmentHistoryItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(1.dp, SoftLine, RoundedCornerShape(22.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.date.dateWithWeekday()} ${item.time.format(AppointmentTimeFormatter)}",
                        color = InkColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "同行：${item.companionName}",
                        color = MutedInk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = item.result,
                    color = InkColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .background(AccentYellow, RoundedCornerShape(7.dp))
                        .border(1.dp, SoftLine, RoundedCornerShape(7.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun FramedPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(1.dp, SoftLine, RoundedCornerShape(28.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun DetailBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = MutedInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(value, color = InkColor, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
    }
}

@Composable
private fun YellowActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentYellow, contentColor = InkColor),
        border = BorderStroke(1.dp, SoftLine),
        elevation = null,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = InkColor)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontWeight = FontWeight.Black, letterSpacing = 0.sp, maxLines = 1)
    }
}

@Composable
private fun WhiteActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = WarmPanel, contentColor = InkColor),
        border = BorderStroke(1.dp, SoftLine),
        elevation = null,
    ) {
        if (text.contains("取消")) {
            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = InkColor)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text, fontWeight = FontWeight.Black, letterSpacing = 0.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun HaircutProposal.dateTimeText(): String = "${proposedDate.dateWithWeekday()} ${proposedTime.format(AppointmentTimeFormatter)}"

private fun LocalDate.dateWithWeekday(): String = "${format(AppointmentDateFormatter)} ${dayOfWeek.weekdayLabel()}"

private fun DayOfWeek.weekdayLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周日"
}

private fun recommendedDate(averageIntervalDays: Int): LocalDate {
    val interval = averageIntervalDays.takeIf { it > 0 } ?: 31
    return LocalDate.now().plusDays((interval - 23).coerceAtLeast(1).toLong())
}

private fun nextWeekendDate(): LocalDate {
    var date = LocalDate.now().plusDays(1)
    while (date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY) {
        date = date.plusDays(1)
    }
    return date
}

private fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private enum class AppointmentFlowStep(val label: String) {
    Date("日期"),
    Time("时间"),
    Send("发送"),
}

@Preview(showBackground = true, widthDp = 236, heightDp = 700)
@Composable
private fun AppointmentPreview() {
    JitouTheme {
        AppointmentRoute(
            proposal = HaircutProposal(
                id = "preview",
                proposedDate = LocalDate.of(2026, 5, 10),
                proposedTime = LocalTime.of(15, 0),
                proposerName = "XX",
                status = ProposalStatus.Confirmed,
            ),
            historyItems = listOf(
                AppointmentHistoryItem(
                    id = "history-preview",
                    date = LocalDate.of(2026, 4, 9),
                    time = LocalTime.of(20, 30),
                    companionName = "阿杰",
                    result = "已完成",
                ),
            ),
            averageIntervalDays = 31,
            friendDaysSinceLast = 18,
            friendName = "阿杰",
            isFriendQueueing = true,
            onBack = {},
            onProposalChange = {},
            onHistoryAdd = {},
            onCompleteHaircut = {},
        )
    }
}
