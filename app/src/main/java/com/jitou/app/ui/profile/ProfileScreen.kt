package com.jitou.app.ui.profile

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.jitou.app.model.HaircutRecord
import com.jitou.app.model.fakeHaircutRecords
import com.jitou.app.model.zhLabel
import com.jitou.app.ui.theme.JitouTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val ProfileBackground = Color(0xFFF8F7F2)
private val ProfileInk = Color(0xFF171717)
private val ProfileYellow = Color(0xFFFFD84D)
private val ProfileMint = Color(0xFFCFECE1)
private val ProfileWarmPanel = Color(0xFFF0ECE2)
private val ProfileMuted = Color(0xFF72706A)
private val ProfileSoftLine = Color(0x14000000)
private val MonthDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd")

@Composable
fun ProfileRoute(
    records: List<HaircutRecord>,
    onBack: () -> Unit,
    onAddRecord: () -> Unit,
    onReminderClick: () -> Unit,
) {
    val stats = ProfileStats.from(records)
    var showAccountSettings by remember { mutableStateOf(false) }

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
            UserInfoCard(
                averageIntervalDays = stats.averageIntervalDays,
                onClick = { showAccountSettings = true },
            )
            FeatureEntryGrid(
                onDataClick = {},
                onAddRecord = onAddRecord,
                onReminderClick = onReminderClick,
                onAboutClick = {},
            )
            CoreStatsGrid(stats = stats)
            RecentIntervalsPanel(intervals = stats.recentIntervals)
            WeekdayFrequencyPanel(items = stats.weekdayFrequencies)
            TrendHint(stats = stats)
            Spacer(modifier = Modifier.height(104.dp))
        }
    }

    if (showAccountSettings) {
        AccountSettingsDialog(
            onDismiss = { showAccountSettings = false },
            onChangeNickname = { showAccountSettings = false },
            onLogout = { showAccountSettings = false },
        )
    }
}

@Composable
private fun UserInfoCard(
    averageIntervalDays: Int,
    onClick: () -> Unit,
) {
    FramedCard(
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(ProfileYellow, CircleShape)
                    .border(2.dp, ProfileInk, CircleShape)
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("昵称：Sion", color = ProfileInk, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text(
                    text = "我的剪头周期：平均 ${averageIntervalDays.ifZeroPlaceholder()} 天",
                    color = ProfileMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AccountSettingsDialog(
    onDismiss: () -> Unit,
    onChangeNickname: () -> Unit,
    onLogout: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("个人账户设置", color = ProfileInk, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AccountActionButton(text = "更改昵称", onClick = onChangeNickname)
                AccountActionButton(text = "退出登录", onClick = onLogout)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
    )
}

@Composable
private fun AccountActionButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
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
    onAboutClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureEntry("数据剪头", Icons.Rounded.BarChart, onDataClick, Modifier.weight(1f))
            FeatureEntry("补录头期", Icons.Rounded.Add, onAddRecord, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeatureEntry("提醒设置", Icons.Rounded.Notifications, onReminderClick, Modifier.weight(1f))
            FeatureEntry("关于几头", Icons.Rounded.Info, onAboutClick, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FeatureEntry(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ProfileInk),
        border = BorderStroke(1.dp, ProfileSoftLine),
        elevation = null,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = ProfileInk)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp, maxLines = 1)
    }
}

@Composable
private fun CoreStatsGrid(stats: ProfileStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("历史平均间隔", "${stats.averageIntervalDays.ifZeroPlaceholder()} 天剪一次", Modifier.weight(1f))
            StatCard("最近一次间隔", "${stats.latestIntervalDays.ifZeroPlaceholder()} 天", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("最长间隔", "${stats.longestIntervalDays.ifZeroPlaceholder()} 天", Modifier.weight(1f))
            StatCard("最短间隔", "${stats.shortestIntervalDays.ifZeroPlaceholder()} 天", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(88.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, ProfileSoftLine, RoundedCornerShape(24.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, color = ProfileMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = ProfileInk, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
    }
}

@Composable
private fun RecentIntervalsPanel(intervals: List<IntervalUiItem>) {
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
private fun WeekdayFrequencyPanel(items: List<WeekdayFrequency>) {
    val maxCount = items.maxOfOrNull { it.count } ?: 1

    FramedCard {
        Text("最常剪头的日子", color = ProfileInk, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    item.day.zhLabel(),
                    color = ProfileInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(36.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .background(ProfileWarmPanel, RoundedCornerShape(9.dp))
                        .border(1.dp, ProfileSoftLine, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((item.count.toFloat() / maxCount).coerceIn(0.08f, 1f))
                            .height(18.dp)
                            .background(ProfileMint, RoundedCornerShape(9.dp)),
                    )
                }
                Text("${item.count} 次", color = ProfileInk, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun TrendHint(stats: ProfileStats) {
    val hint = when {
        stats.latestIntervalDays == 0 -> "趋势提示：再记录几次，几头就能给你更准的周期判断。"
        stats.latestIntervalDays > stats.averageIntervalDays -> "趋势提示：最近这次比平均周期更久，下次可以提前一点约。"
        stats.latestIntervalDays < stats.averageIntervalDays -> "趋势提示：最近剪得更勤，当前周期保持得不错。"
        else -> "趋势提示：你的剪头节奏很稳定。"
    }

    FramedCard(backgroundColor = ProfileYellow) {
        Text(hint, color = ProfileInk, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
    }
}

@Composable
private fun FramedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(28.dp))
            .border(1.dp, ProfileSoftLine, RoundedCornerShape(28.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

private data class ProfileStats(
    val averageIntervalDays: Int,
    val latestIntervalDays: Int,
    val longestIntervalDays: Int,
    val shortestIntervalDays: Int,
    val recentIntervals: List<IntervalUiItem>,
    val weekdayFrequencies: List<WeekdayFrequency>,
) {
    companion object {
        fun from(records: List<HaircutRecord>): ProfileStats {
            val sorted = records.sortedBy { it.date }
            val intervals = sorted.zipWithNext { previous, next ->
                IntervalUiItem(
                    from = previous.date,
                    to = next.date,
                    days = ChronoUnit.DAYS.between(previous.date, next.date).toInt(),
                )
            }
            val frequencies = sorted
                .groupingBy { it.date.dayOfWeek }
                .eachCount()
                .entries
                .sortedWith(compareByDescending<Map.Entry<DayOfWeek, Int>> { it.value }.thenBy { it.key.value })
                .take(4)
                .map { WeekdayFrequency(day = it.key, count = it.value) }

            return ProfileStats(
                averageIntervalDays = HaircutAnalytics.calculate(records).averageIntervalDays,
                latestIntervalDays = intervals.lastOrNull()?.days ?: 0,
                longestIntervalDays = intervals.maxOfOrNull { it.days } ?: 0,
                shortestIntervalDays = intervals.minOfOrNull { it.days } ?: 0,
                recentIntervals = intervals.takeLast(5).asReversed(),
                weekdayFrequencies = frequencies,
            )
        }
    }
}

private data class IntervalUiItem(
    val from: LocalDate,
    val to: LocalDate,
    val days: Int,
)

private data class WeekdayFrequency(
    val day: DayOfWeek,
    val count: Int,
)

private fun Int.ifZeroPlaceholder(): String = if (this == 0) "--" else toString()

@Preview(showBackground = true, widthDp = 236, heightDp = 760)
@Composable
private fun ProfilePreview() {
    JitouTheme {
        ProfileRoute(
            records = fakeHaircutRecords(LocalDate.of(2026, 5, 2)),
            onBack = {},
            onAddRecord = {},
            onReminderClick = {},
        )
    }
}
