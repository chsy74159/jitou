package com.jitou.app.ui.profile

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitou.app.R
import com.jitou.app.model.HaircutAnalytics
import com.jitou.app.model.HaircutHistoryEntry
import com.jitou.app.model.HaircutInterval
import com.jitou.app.model.HaircutRecord
import com.jitou.app.model.fakeHaircutRecords
import com.jitou.app.ui.theme.JitouTheme
import com.jitou.app.ui.theme.JitouThemeMode
import com.jitou.app.ui.theme.jitouColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ProfileBackground: Color
    @Composable get() = MaterialTheme.jitouColors.background
private val ProfileSurface: Color
    @Composable get() = MaterialTheme.jitouColors.surface
private val ProfileInk: Color
    @Composable get() = MaterialTheme.jitouColors.ink
private val ProfileWarmPanel: Color
    @Composable get() = MaterialTheme.jitouColors.surfaceMuted
private val ProfileMuted: Color
    @Composable get() = MaterialTheme.jitouColors.mutedInk
private val ProfileSoftLine: Color
    @Composable get() = MaterialTheme.jitouColors.line
private val ProfileAvatarLine: Color
    @Composable get() = MaterialTheme.jitouColors.accent.copy(alpha = 0.5f)
private val ProfileAccent: Color
    @Composable get() = MaterialTheme.jitouColors.accent
private val ProfileAccentStrong: Color
    @Composable get() = MaterialTheme.jitouColors.accentStrong
private val ProfileSage: Color
    @Composable get() = MaterialTheme.jitouColors.sage
private val ProfileDanger: Color
    @Composable get() = MaterialTheme.jitouColors.danger
private val MonthDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd")
private val HistoryDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

@Composable
fun ProfileRoute(
    records: List<HaircutRecord>,
    nickname: String,
    nicknameError: String?,
    isActive: Boolean = true,
    onBack: () -> Unit,
    onAddRecord: () -> Unit,
    onReminderClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onRefreshData: () -> Unit,
    isRefreshingData: Boolean,
    themeMode: JitouThemeMode = JitouThemeMode.default,
    onThemeModeChange: (JitouThemeMode) -> Unit = {},
    onLogout: () -> Unit,
) {
    val stats = ProfileStats.from(records)
    var showAccountSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    BackHandler(enabled = showHistory && isActive) {
        showHistory = false
    }

    if (showHistory) {
        HaircutHistoryScreen(
            records = records,
            onBack = { showHistory = false },
        )
    } else {
        Scaffold(contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ProfileBackground)
                    .padding(padding)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProfileThemeMenu(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    modifier = Modifier.align(Alignment.End),
                )
                UserInfoCard(
                    nickname = nickname,
                    daysSinceLastHaircut = stats.daysSinceLastHaircut,
                    onClick = { showAccountSettings = true },
                )
                FeatureEntryGrid(
                    onDataClick = { showHistory = true },
                    onAddRecord = onAddRecord,
                    onReminderClick = onReminderClick,
                    onRefreshData = onRefreshData,
                    isRefreshingData = isRefreshingData,
                )
                ProfileStatsCard(nickname = nickname, stats = stats)
                RecentIntervalsPanel(intervals = stats.recentIntervals)
                Spacer(modifier = Modifier.height(104.dp))
            }
        }
    }

    if (showAccountSettings) {
        AccountSettingsDialog(
            nickname = nickname,
            nicknameError = nicknameError,
            onDismiss = { showAccountSettings = false },
            onChangeNickname = onNicknameChange,
            onLogout = {
                showAccountSettings = false
                onLogout()
            },
        )
    }
}

@Composable
private fun ProfileThemeMenu(
    themeMode: JitouThemeMode,
    onThemeModeChange: (JitouThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .size(30.dp)
                .background(ProfileWarmPanel, CircleShape)
                .border(1.dp, ProfileSoftLine, CircleShape),
        ) {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = "主题设置",
                tint = ProfileAccentStrong,
                modifier = Modifier.size(20.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ProfileSurface,
        ) {
            JitouThemeMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = mode.displayLabel(),
                            color = ProfileInk,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.sp,
                        )
                    },
                    leadingIcon = {
                        if (mode == themeMode) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = ProfileAccentStrong,
                                modifier = Modifier.size(18.dp),
                            )
                        } else {
                            Spacer(modifier = Modifier.size(18.dp))
                        }
                    },
                    onClick = {
                        expanded = false
                        onThemeModeChange(mode)
                    },
                )
            }
        }
    }
}

private fun JitouThemeMode.displayLabel(): String = when (this) {
    JitouThemeMode.Light -> "浅色"
    JitouThemeMode.Dark -> "深色"
    JitouThemeMode.System -> "跟随系统"
}

@Composable
private fun UserInfoCard(
    nickname: String,
    daysSinceLastHaircut: Int?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 0.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(108.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .border(2.dp, ProfileAvatarLine, CircleShape)
                    .padding(2.dp),
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
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-2).dp, y = 3.dp)
                    .size(24.dp)
                    .background(ProfileWarmPanel, CircleShape)
                    .border(1.dp, ProfileAvatarLine, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = daysSinceLastHaircut?.toString() ?: "--",
                    color = ProfileMuted,
                    fontSize = if ((daysSinceLastHaircut ?: 0) > 99) 9.sp else 11.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                    maxLines = 1,
                )
            }
        }
        Text(
            text = nickname,
            color = ProfileInk,
            fontSize = 23.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
    }
}

@Composable
private fun HaircutHistoryScreen(
    records: List<HaircutRecord>,
    onBack: () -> Unit,
) {
    val entries = remember(records) { HaircutAnalytics.historyEntries(records) }

    Scaffold(contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ProfileBackground)
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "返回",
                        tint = ProfileInk,
                    )
                }
                Text(
                    text = "历史剪头",
                    color = ProfileInk,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                )
            }

            if (entries.isEmpty()) {
                EmptyHistoryState()
            } else {
                entries.forEach { entry ->
                    HaircutHistoryRow(entry = entry)
                }
            }

            Spacer(modifier = Modifier.height(104.dp))
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .background(ProfileSurface, RoundedCornerShape(18.dp))
            .border(1.dp, ProfileSoftLine, RoundedCornerShape(18.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("还没有剪头记录", color = ProfileInk, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text("补录一次后会出现在这里", color = ProfileMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HaircutHistoryRow(entry: HaircutHistoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .background(ProfileSurface, RoundedCornerShape(16.dp))
            .border(1.dp, ProfileSoftLine, RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = entry.date.format(HistoryDateFormatter),
            color = ProfileInk,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
        if (entry.isLatest) {
            Text(
                text = "Latest",
                color = ProfileAccentStrong,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
                modifier = Modifier
                    .background(ProfileSage, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        } else {
            Text(
                text = "${entry.daysAgo} days ago",
                color = ProfileMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
        }
    }
}

@Composable
private fun AccountSettingsDialog(
    nickname: String,
    nicknameError: String?,
    onDismiss: () -> Unit,
    onChangeNickname: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var nicknameDraft by remember(nickname) { mutableStateOf(nickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("个人账户设置", color = ProfileInk, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nicknameDraft,
                    onValueChange = { nicknameDraft = it },
                    label = { Text("昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                nicknameError?.let { message ->
                    Text(message, color = ProfileDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                AccountActionButton(
                    text = "保存昵称",
                    enabled = nicknameDraft.isNotBlank(),
                    onClick = { onChangeNickname(nicknameDraft) },
                )
                AccountActionButton(text = "退出登录", onClick = onLogout)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = ProfileSurface,
    )
}

@Composable
private fun AccountActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ProfileWarmPanel, contentColor = ProfileInk),
        border = BorderStroke(1.dp, ProfileSoftLine),
        elevation = null,
    ) {
        Text(text, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
    }
}

@Composable
private fun FeatureEntryGrid(
    onDataClick: () -> Unit,
    onAddRecord: () -> Unit,
    onReminderClick: () -> Unit,
    onRefreshData: () -> Unit,
    isRefreshingData: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureEntry("历史剪头", Icons.Rounded.History, onDataClick, Modifier.weight(1f))
            FeatureEntry("补录头期", Icons.Rounded.Add, onAddRecord, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureEntry("提醒设置", Icons.Rounded.Notifications, onReminderClick, Modifier.weight(1f))
            FeatureEntry(
                text = if (isRefreshingData) "刷新中" else "刷新数据",
                icon = Icons.Rounded.Refresh,
                onClick = onRefreshData,
                modifier = Modifier.weight(1f),
                enabled = !isRefreshingData,
            )
        }
    }
}

@Composable
private fun FeatureEntry(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ProfileSurface, contentColor = ProfileInk),
        border = BorderStroke(1.dp, ProfileSoftLine),
        elevation = null,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = ProfileInk)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp, maxLines = 1)
    }
}

@Composable
private fun ProfileStatsCard(
    nickname: String,
    stats: ProfileStats,
) {
    FramedCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(ProfileSage, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = nickname.take(2).uppercase(),
                    color = ProfileAccentStrong,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                )
            }
            Text("你的剪头数据", color = ProfileInk, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactMetricTile(
                label = "平均间隔",
                value = "${stats.averageIntervalDays.ifZeroPlaceholder()}天",
                modifier = Modifier.weight(1f),
            )
            CompactMetricTile(
                label = "总剪头",
                value = "${stats.totalCuts}",
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactMetricTile(
                label = "最近间隔",
                value = "${stats.latestIntervalDays.ifZeroPlaceholder()}天",
                modifier = Modifier.weight(1f),
            )
            CompactMetricTile(
                label = "最长 / 最短",
                value = "${stats.longestIntervalDays.ifZeroPlaceholder()} / ${stats.shortestIntervalDays.ifZeroPlaceholder()}天",
                modifier = Modifier.weight(1f),
            )
        }

        Text("按星期分布", color = ProfileMuted, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
        WeekdayBarChart(items = stats.weekdayFrequencies)
    }
}

@Composable
private fun CompactMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(min = 84.dp)
            .background(ProfileWarmPanel, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Text(
            label,
            color = ProfileMuted,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Text(
            value,
            color = ProfileAccentStrong,
            fontSize = if (value.length > 6) 17.sp else 20.sp,
            lineHeight = if (value.length > 6) 21.sp else 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun RecentIntervalsPanel(intervals: List<HaircutInterval>) {
    FramedCard {
        Text("近 5 次剪头间隔", color = ProfileInk, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
        intervals.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${item.from.format(MonthDayFormatter)} → ${item.to.format(MonthDayFormatter)}",
                    color = ProfileInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text("${item.days} 天", color = ProfileInk, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun WeekdayBarChart(items: List<WeekdayFrequency>) {
    val maxCount = items.maxOfOrNull { it.count } ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        items.forEach { item ->
            val barHeight = if (item.count == 0) 2.dp else ((item.count.toFloat() / maxCount) * 58).dp.coerceAtLeast(14.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Text(
                            if (item.count == 0) " " else item.count.toString(),
                            color = ProfileMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.sp,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .height(barHeight)
                                .background(
                                    if (item.count == 0) Color.Transparent else ProfileAccent,
                                    RoundedCornerShape(6.dp),
                                ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.day.shortLabel(), color = ProfileMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun FramedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardColor = backgroundColor ?: ProfileSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(cardColor, RoundedCornerShape(28.dp))
            .border(1.dp, ProfileSoftLine, RoundedCornerShape(28.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

private data class ProfileStats(
    val totalCuts: Int,
    val averageIntervalDays: Int,
    val latestIntervalDays: Int,
    val longestIntervalDays: Int,
    val shortestIntervalDays: Int,
    val daysSinceLastHaircut: Int?,
    val recentIntervals: List<HaircutInterval>,
    val weekdayFrequencies: List<WeekdayFrequency>,
) {
    companion object {
        fun from(records: List<HaircutRecord>): ProfileStats {
            val sorted = records.sortedBy { it.date }
            val analytics = HaircutAnalytics.calculate(records)
            val intervals = HaircutAnalytics.calculateIntervals(records)
            val countsByDay = sorted
                .groupingBy { it.date.dayOfWeek }
                .eachCount()
            val frequencies = listOf(
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
            ).map { day -> WeekdayFrequency(day = day, count = countsByDay[day] ?: 0) }

            return ProfileStats(
                totalCuts = records.size,
                averageIntervalDays = analytics.averageIntervalDays,
                latestIntervalDays = intervals.lastOrNull()?.days ?: 0,
                longestIntervalDays = intervals.maxOfOrNull { it.days } ?: 0,
                shortestIntervalDays = intervals.minOfOrNull { it.days } ?: 0,
                daysSinceLastHaircut = HaircutAnalytics.daysSinceLastHaircut(records),
                recentIntervals = intervals.takeLast(5).asReversed(),
                weekdayFrequencies = frequencies,
            )
        }
    }
}

private data class WeekdayFrequency(
    val day: DayOfWeek,
    val count: Int,
)

private fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.SUNDAY -> "Sun"
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    DayOfWeek.SATURDAY -> "Sat"
}

private fun Int.ifZeroPlaceholder(): String = if (this == 0) "--" else toString()

@Preview(showBackground = true, widthDp = 236, heightDp = 760)
@Composable
private fun ProfilePreview() {
    JitouTheme {
        ProfileRoute(
            records = fakeHaircutRecords(LocalDate.of(2026, 5, 2)),
            nickname = "Sion",
            nicknameError = null,
            onBack = {},
            onAddRecord = {},
            onReminderClick = {},
            onNicknameChange = {},
            onRefreshData = {},
            isRefreshingData = false,
            onLogout = {},
        )
    }
}
