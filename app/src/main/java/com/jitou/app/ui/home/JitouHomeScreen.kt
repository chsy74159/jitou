package com.jitou.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitou.app.R
import com.jitou.app.data.local.JitouDatabase
import com.jitou.app.data.repository.JitouRepository
import com.jitou.app.model.HaircutAnalytics
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.HaircutRecord
import com.jitou.app.model.ProposalStatus
import com.jitou.app.model.QueueEvent
import com.jitou.app.model.QueueState
import com.jitou.app.model.ReminderUiState
import com.jitou.app.model.fakeHaircutRecords
import com.jitou.app.model.fakeReminderState
import com.jitou.app.model.zhLabel
import com.jitou.app.ui.appointment.AppointmentRoute
import com.jitou.app.ui.profile.ProfileRoute
import com.jitou.app.ui.theme.JitouTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val HomeBackground = Color(0xFFF8F7F2)
private val Ink = Color(0xFF171717)
private val MutedInk = Color(0xFF72706A)
private val Yellow = Color(0xFFFFD84D)
private val Mint = Color(0xFFCFECE1)
private val Peach = Color(0xFFFFD8C9)
private val WarmPanel = Color(0xFFF0ECE2)
private val SoftLine = Color(0x14000000)

@Composable
fun JitouHomeRoute() {
    val context = LocalContext.current
    val repository = remember(context) {
        JitouRepository(JitouDatabase.getInstance(context))
    }
    val coroutineScope = rememberCoroutineScope()
    val records by repository.haircutRecords.collectAsState(initial = emptyList())
    val proposal by repository.activeProposal.collectAsState(initial = null)
    val reminder by repository.reminderState.collectAsState(initial = fakeReminderState())
    val appointmentHistory by repository.appointmentHistory.collectAsState(initial = emptyList())
    var screen by remember { mutableStateOf(JitouScreen.Home) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var isQueueing by remember { mutableStateOf(false) }
    var showJoinQueueDialog by remember { mutableStateOf(false) }
    var showCancelQueueDialog by remember { mutableStateOf(false) }

    LaunchedEffect(repository) {
        repository.seedDefaultsIfNeeded()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (screen) {
            JitouScreen.Home -> {
                JitouHomeScreen(
                    records = records,
                    proposal = proposal,
                    reminder = reminder,
                    isQueueing = isQueueing,
                    onRecordClick = { showRecordDialog = true },
                    onAppointmentClick = { screen = JitouScreen.Appointment },
                    onProfileClick = { screen = JitouScreen.Profile },
                    onQueueClick = {
                        if (isQueueing) {
                            showCancelQueueDialog = true
                        } else {
                            showJoinQueueDialog = true
                        }
                    },
                    onReminderClick = { showReminderSheet = true },
                )
            }

            JitouScreen.Appointment -> {
                AppointmentRoute(
                    proposal = proposal,
                    historyItems = appointmentHistory,
                    averageIntervalDays = HaircutAnalytics.calculate(records).averageIntervalDays,
                    friendDaysSinceLast = HaircutAnalytics.daysSinceLastHaircut(records) ?: 0,
                    friendName = "XX",
                    isFriendQueueing = QueueState.shouldShowFriendQueueNotice(isQueueing, proposal?.status),
                    onBack = { screen = JitouScreen.Home },
                    onProposalChange = { nextProposal ->
                        coroutineScope.launch {
                            repository.setActiveProposal(nextProposal)
                        }
                    },
                    onHistoryAdd = { item ->
                        coroutineScope.launch {
                            repository.addAppointmentHistory(item)
                        }
                    },
                    onCompleteHaircut = { completedDate ->
                        coroutineScope.launch {
                        repository.addHaircutRecord(completedDate, note = "和朋友一起")
                        repository.setActiveProposal(null)
                    }
                    isQueueing = QueueState.reduce(isQueueing, QueueEvent.RecordedHaircut)
                    screen = JitouScreen.Home
                },
            )
            }

            JitouScreen.Profile -> {
                ProfileRoute(
                    records = records,
                    onBack = { screen = JitouScreen.Home },
                    onAddRecord = { showRecordDialog = true },
                    onReminderClick = { showReminderSheet = true },
                )
            }
        }

        JitouBottomNav(
            selected = screen,
            onSelect = { screen = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showRecordDialog) {
        RecordHaircutDialog(
            onDismiss = { showRecordDialog = false },
            onConfirm = { date ->
                coroutineScope.launch {
                    repository.addHaircutRecord(date)
                }
                isQueueing = QueueState.reduce(isQueueing, QueueEvent.RecordedHaircut)
                showRecordDialog = false
            },
        )
    }

    if (showReminderSheet) {
        ReminderBottomSheet(
            reminder = reminder,
            onReminderChange = { nextReminder ->
                coroutineScope.launch {
                    repository.setReminder(nextReminder)
                }
            },
            onDismiss = { showReminderSheet = false },
        )
    }

    if (showJoinQueueDialog) {
        QueueConfirmDialog(
            title = "是否完成排队",
            confirmText = "已排队",
            onDismiss = { showJoinQueueDialog = false },
            onConfirm = {
                isQueueing = QueueState.reduce(isQueueing, QueueEvent.JoinedQueue)
                showJoinQueueDialog = false
            },
        )
    }

    if (showCancelQueueDialog) {
        QueueConfirmDialog(
            title = "是否已取消排队",
            confirmText = "是",
            onDismiss = { showCancelQueueDialog = false },
            onConfirm = {
                isQueueing = QueueState.reduce(isQueueing, QueueEvent.CancelledQueue)
                showCancelQueueDialog = false
            },
        )
    }
}

@Composable
private fun JitouBottomNav(
    selected: JitouScreen,
    onSelect: (JitouScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .shadow(18.dp, RoundedCornerShape(34.dp), ambientColor = Color(0x18000000), spotColor = Color(0x18000000))
            .background(Color(0xF7FFFFFF), RoundedCornerShape(34.dp))
            .border(width = 1.dp, color = Color(0x11000000), shape = RoundedCornerShape(34.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItem(
            label = "首页",
            icon = Icons.Rounded.Home,
            selected = selected == JitouScreen.Home,
            onClick = { onSelect(JitouScreen.Home) },
            modifier = Modifier.weight(1f),
        )
        BottomNavItem(
            label = "约头",
            icon = Icons.Rounded.CalendarMonth,
            selected = selected == JitouScreen.Appointment,
            onClick = { onSelect(JitouScreen.Appointment) },
            modifier = Modifier.weight(1f),
        )
        BottomNavItem(
            label = "我的",
            icon = Icons.Rounded.Person,
            selected = selected == JitouScreen.Profile,
            onClick = { onSelect(JitouScreen.Profile) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (selected) Color(0xFFE9ECEF) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(23.dp),
            tint = if (selected) Color(0xFF6F72FF) else Ink,
        )
        Text(
            text = label,
            color = if (selected) Color(0xFF6F72FF) else Ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun JitouHomeScreen(
    records: List<HaircutRecord>,
    proposal: HaircutProposal?,
    reminder: ReminderUiState,
    isQueueing: Boolean,
    onRecordClick: () -> Unit,
    onAppointmentClick: () -> Unit,
    onProfileClick: () -> Unit,
    onQueueClick: () -> Unit,
    onReminderClick: () -> Unit,
) {
    val today = LocalDate.now()
    val sortedRecords = records.sortedByDescending { it.date }
    val lastRecord = sortedRecords.firstOrNull()
    val daysSinceLast = HaircutAnalytics.daysSinceLastHaircut(records, today) ?: 0
    val stats = HaircutAnalytics.calculate(records)
    val nextDate = proposal?.proposedDate ?: today.plusDays(7)
    val status = haircutStatus(daysSinceLast, stats.averageIntervalDays)

    Scaffold(contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBackground)
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HomeTopBar(onProfileClick = onProfileClick)

            Spacer(modifier = Modifier.height(14.dp))

            AvatarHeroCard(
                daysSinceLast = daysSinceLast,
                status = status,
                reminder = reminder,
                onReminderClick = onReminderClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryStrip(
                lastDate = lastRecord?.date,
                averageIntervalDays = stats.averageIntervalDays,
                nextDate = nextDate,
            )

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryActionRow(
                queueButtonText = QueueState.buttonText(isQueueing),
                onQueueClick = onQueueClick,
                onNowClick = onRecordClick,
            )

            Spacer(modifier = Modifier.height(14.dp))

            CoopPanel(
                proposal = proposal,
                showFriendQueueNotice = QueueState.shouldShowFriendQueueNotice(isQueueing, proposal?.status),
                onClick = onAppointmentClick,
            )

            Spacer(modifier = Modifier.height(104.dp))
        }
    }
}

@Composable
private fun HomeTopBar(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "几头",
                color = Ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
            Text(
                text = "jitou haircut tracker",
                color = MutedInk,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            )
        }
        ProfileChip(onClick = onProfileClick)
    }
}

@Composable
private fun ProfileChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(38.dp)
            .background(Ink, RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
            .padding(start = 7.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.cartoon_avatar),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = "我的",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
    }
}

@Composable
private fun AvatarHeroCard(
    daysSinceLast: Int,
    status: String,
    reminder: ReminderUiState,
    onReminderClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(318.dp)
            .shadow(18.dp, RoundedCornerShape(36.dp), ambientColor = Color(0x18000000), spotColor = Color(0x14000000))
            .background(Color.White, RoundedCornerShape(36.dp))
            .border(width = 1.dp, color = SoftLine, shape = RoundedCornerShape(36.dp)),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 18.dp, start = 18.dp)
                .background(Ink, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = "TODAY",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 18.dp)
                .background(Mint, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = "SOFT CUT",
                color = Ink,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
        }

        Image(
            painter = painterResource(id = R.drawable.cartoon_avatar),
            contentDescription = "卡通头像",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 54.dp)
                .size(178.dp)
                .clip(RoundedCornerShape(42.dp)),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp, top = 56.dp)
                .size(width = 108.dp, height = 108.dp)
                .background(Yellow, RoundedCornerShape(30.dp))
                .border(width = 2.dp, color = Ink, shape = RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = daysSinceLast.toString(),
                    color = Ink,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                )
                Text(
                    text = "天没剪",
                    color = Ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(WarmPanel, RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (reminder.enabled) "提醒 ${reminder.time.toReminderText()}" else "提醒已关闭",
                color = MutedInk,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            )
            StatusPill(status = status, onClick = onReminderClick)
        }
    }
}

@Composable
private fun SummaryStrip(
    lastDate: LocalDate?,
    averageIntervalDays: Int,
    nextDate: LocalDate,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryCapsule(
            label = "上次",
            value = lastDate?.format(DateFormatter) ?: "暂无",
            tint = Peach,
            modifier = Modifier.weight(1f),
        )
        SummaryCapsule(
            label = "均值",
            value = if (averageIntervalDays == 0) "暂无" else "${averageIntervalDays}天",
            tint = Mint,
            modifier = Modifier.weight(1f),
        )
        SummaryCapsule(
            label = "建议下次",
            value = nextDate.format(DateFormatter),
            tint = Yellow,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCapsule(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(78.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x0F000000), spotColor = Color(0x0A000000))
            .background(tint, RoundedCornerShape(24.dp))
            .border(width = 1.dp, color = SoftLine, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            color = Color(0xFF57544E),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        )
        Text(
            text = value,
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusPill(status: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFFF4F1EA), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "头毛状态：$status",
            color = Ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Ink, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Yellow,
            )
        }
    }
}

@Composable
private fun SoftActionButton(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Ink,
        ),
        border = BorderStroke(2.dp, Color(0x12000000)),
        elevation = null,
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun PrimaryActionRow(
    queueButtonText: String,
    onQueueClick: () -> Unit,
    onNowClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(width = 1.dp, color = SoftLine, shape = RoundedCornerShape(28.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "几时头",
                color = Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
            Text(
                text = "tap to update",
                color = MutedInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SoftActionButton(
                text = queueButtonText,
                containerColor = WarmPanel,
                modifier = Modifier.weight(0.95f),
                onClick = onQueueClick,
            )
            SoftActionButton(
                text = "记录剪头",
                containerColor = Yellow,
                modifier = Modifier.weight(1.25f),
                onClick = onNowClick,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCut,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Ink,
                )
            }
        }
    }
}

@Composable
private fun CoopPanel(
    proposal: HaircutProposal?,
    showFriendQueueNotice: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (showFriendQueueNotice) 176.dp else 150.dp)
            .shadow(10.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x14000000), spotColor = Color(0x0F000000))
            .background(Ink, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 14.dp, end = 14.dp)
                .background(Yellow, RoundedCornerShape(15.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "CO-OP",
                color = Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, end = 16.dp, top = 30.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Ink,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (proposal == null) {
                    Text(
                        text = "暂无约头计划",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SmallOutlinedLabel("双方还没有发起日程")
                    SmallYellowLabel("发起约剪")
                } else {
                    Text(
                        text = "和XX的剪头计划",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SmallOutlinedLabel("初定时间：${proposal.proposedDate.format(DateFormatter)}")
                    SmallYellowLabel("状态：${proposal.statusLabel()}")
                    if (showFriendQueueNotice) {
                        SmallOutlinedLabel(QueueState.queueNoticeText(isOwnCard = true))
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallOutlinedLabel(text: String) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .background(Color(0xFF2A2A2A), RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SmallYellowLabel(text: String) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .background(Yellow, RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AnalysisPanel(
    averageIntervalDays: Int,
    recentIntervals: List<Int>,
    mostFrequentWeekday: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(width = 1.dp, color = SoftLine, shape = RoundedCornerShape(28.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "数据分析",
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
        AnalysisLine("历史平均", if (averageIntervalDays == 0) "暂无" else "$averageIntervalDays 天")
        AnalysisLine("近几次间隔", recentIntervals.ifEmpty { listOf(0) }.joinToString(" / ") { if (it == 0) "暂无" else "${it}天" })
        AnalysisLine("高频星期", mostFrequentWeekday)
    }
}

@Composable
private fun AnalysisLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarmPanel, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color(0xFF6E6E6E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End)
    }
}

@Composable
private fun QueueConfirmDialog(
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
private fun RecordHaircutDialog(
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
private fun ReminderBottomSheet(
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

private fun HaircutProposal.statusLabel(): String = when (status) {
    ProposalStatus.PendingFriend -> "待确认"
    ProposalStatus.PendingMe -> "待我确认"
    ProposalStatus.Confirmed -> "已达成"
}

private fun haircutStatus(daysSinceLast: Int, averageIntervalDays: Int): String = when {
    averageIntervalDays == 0 -> "先记一剪"
    daysSinceLast >= averageIntervalDays + 5 -> "该剪了"
    daysSinceLast >= averageIntervalDays - 8 -> "差不多该约了"
    else -> "还撑得住"
}

private fun LocalTime.toReminderText(): String = "%02d:%02d".format(hour, minute)

private fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Preview(showBackground = true, widthDp = 236, heightDp = 604)
@Composable
private fun JitouHomePreview() {
    JitouTheme {
        JitouHomeScreen(
            records = fakeHaircutRecords(LocalDate.of(2026, 4, 30)),
            proposal = HaircutProposal(
                id = "proposal-preview",
                proposedDate = LocalDate.of(2026, 5, 8),
                proposedTime = LocalTime.of(15, 0),
                proposerName = "XX",
                status = ProposalStatus.Confirmed,
            ),
            reminder = fakeReminderState(),
            isQueueing = false,
            onRecordClick = {},
            onAppointmentClick = {},
            onProfileClick = {},
            onQueueClick = {},
            onReminderClick = {},
        )
    }
}

private enum class JitouScreen {
    Home,
    Appointment,
    Profile,
}
