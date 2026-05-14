package com.jitou.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jitou.app.model.HaircutAnalytics
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.HaircutRecord
import com.jitou.app.model.ProposalStatus
import com.jitou.app.model.QueueState
import com.jitou.app.model.fakeHaircutRecords
import com.jitou.app.ui.theme.JitouTheme
import java.time.LocalDate
import java.time.LocalTime

@Composable
internal fun JitouHomeScreen(
    records: List<HaircutRecord>,
    proposal: HaircutProposal?,
    isQueueing: Boolean,
    friendName: String,
    onRecordClick: () -> Unit,
    onAppointmentClick: () -> Unit,
    onProfileClick: () -> Unit,
    onQueueClick: () -> Unit,
) {
    val today = LocalDate.now()
    val sortedRecords = records.sortedByDescending { it.date }
    val lastRecord = sortedRecords.firstOrNull()
    val daysSinceLast = HaircutAnalytics.daysSinceLastHaircut(records, today)
    val stats = HaircutAnalytics.calculate(records)
    val nextDate = proposal?.proposedDate ?: today.plusDays(7)
    val status = haircutStatus(daysSinceLast ?: 0, stats.averageIntervalDays)

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

            HairIllustrationHero(
                daysSinceLast = daysSinceLast,
                status = status,
                todayDate = today,
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
                friendName = friendName,
                showFriendQueueNotice = QueueState.shouldShowFriendQueueNotice(isQueueing, proposal?.status),
                onClick = onAppointmentClick,
            )

            Spacer(modifier = Modifier.height(104.dp))
        }
    }
}

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
            isQueueing = false,
            friendName = "阿杰",
            onRecordClick = {},
            onAppointmentClick = {},
            onProfileClick = {},
            onQueueClick = {},
        )
    }
}
