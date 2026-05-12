package com.jitou.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jitou.app.model.HaircutAnalytics
import com.jitou.app.model.QueueState
import com.jitou.app.ui.appointment.AppointmentRoute
import com.jitou.app.ui.login.LoginScreen
import com.jitou.app.ui.login.LoginViewModel
import com.jitou.app.ui.profile.ProfileRoute

@Composable
fun JitouHomeRoute() {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel(
        factory = remember(context) { LoginViewModel.factory(context) },
    )
    val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    if (!loginUiState.isAuthenticated) {
        LoginScreen(
            uiState = loginUiState,
            onSignIn = loginViewModel::signIn,
        )
        return
    }

    val viewModel: JitouHomeViewModel = viewModel(
        factory = remember(context) { JitouHomeViewModel.factory(context) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val records = uiState.records
    val proposal = uiState.proposal
    val reminder = uiState.reminder
    val appointmentHistory = uiState.appointmentHistory
    val isQueueing = uiState.isQueueing
    val friendName = uiState.friendName
    var screen by remember { mutableStateOf(JitouScreen.Home) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showJoinQueueDialog by remember { mutableStateOf(false) }
    var showCancelQueueDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (screen) {
            JitouScreen.Home -> {
                JitouHomeScreen(
                    records = records,
                    proposal = proposal,
                    reminder = reminder,
                    isQueueing = isQueueing,
                    friendName = friendName,
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
                    friendName = friendName,
                    isFriendQueueing = QueueState.shouldShowFriendQueueNotice(isQueueing, proposal?.status),
                    onBack = { screen = JitouScreen.Home },
                    onProposalChange = viewModel::setActiveProposal,
                    onHistoryAdd = viewModel::addAppointmentHistory,
                    onCompleteHaircut = { completedDate ->
                        viewModel.completeFriendHaircut(completedDate)
                        screen = JitouScreen.Home
                    },
                )
            }

            JitouScreen.Profile -> {
                ProfileRoute(
                    records = records,
                    nickname = uiState.nickname,
                    nicknameError = uiState.nicknameError,
                    onBack = { screen = JitouScreen.Home },
                    onAddRecord = { showRecordDialog = true },
                    onReminderClick = { showReminderSheet = true },
                    onNicknameChange = viewModel::updateNickname,
                    onRefreshData = viewModel::refreshData,
                    isRefreshingData = uiState.isRefreshingData,
                    onLogout = {
                        screen = JitouScreen.Home
                        loginViewModel.signOut()
                    },
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
                viewModel.addHaircutRecord(date)
                showRecordDialog = false
            },
        )
    }

    if (showReminderSheet) {
        ReminderBottomSheet(
            reminder = reminder,
            onReminderChange = viewModel::setReminder,
            onDismiss = { showReminderSheet = false },
        )
    }

    if (showJoinQueueDialog) {
        QueueConfirmDialog(
            title = "是否完成排队",
            confirmText = "已排队",
            onDismiss = { showJoinQueueDialog = false },
            onConfirm = {
                viewModel.joinQueue()
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
                viewModel.cancelQueue()
                showCancelQueueDialog = false
            },
        )
    }
}
