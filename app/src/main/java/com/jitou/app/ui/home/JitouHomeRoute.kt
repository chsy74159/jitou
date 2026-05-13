package com.jitou.app.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.jitou.app.ui.theme.JitouThemeMode
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun JitouHomeRoute(
    themeMode: JitouThemeMode = JitouThemeMode.default,
    onThemeModeChange: (JitouThemeMode) -> Unit = {},
) {
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
    val screens = remember { JitouScreen.primaryScreens() }
    val pagerState = rememberPagerState(initialPage = JitouScreen.Home.pageIndex) { screens.size }
    val coroutineScope = rememberCoroutineScope()
    val screen by remember {
        derivedStateOf { JitouScreen.fromPageIndex(pagerState.currentPage) }
    }
    val navPosition by remember {
        derivedStateOf {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, (screens.size - 1).toFloat())
        }
    }
    val navSelectedScreen by remember {
        derivedStateOf { JitouScreen.fromPageIndex(navPosition.roundToInt()) }
    }
    var showRecordDialog by remember { mutableStateOf(false) }
    var recordDialogPastDatesOnly by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showJoinQueueDialog by remember { mutableStateOf(false) }
    var showCancelQueueDialog by remember { mutableStateOf(false) }
    val navigateToScreen: (JitouScreen) -> Unit = { destination ->
        coroutineScope.launch {
            pagerState.animateScrollToPage(destination.pageIndex)
        }
    }

    BackHandler(enabled = screen.systemBackTarget() != null) {
        screen.systemBackTarget()?.let(navigateToScreen)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (screens[page]) {
                JitouScreen.Home -> {
                    JitouHomeScreen(
                        records = records,
                        proposal = proposal,
                        reminder = reminder,
                        isQueueing = isQueueing,
                        friendName = friendName,
                        onRecordClick = {
                            recordDialogPastDatesOnly = false
                            showRecordDialog = true
                        },
                        onAppointmentClick = { navigateToScreen(JitouScreen.Appointment) },
                        onProfileClick = { navigateToScreen(JitouScreen.Profile) },
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
                        onBack = { navigateToScreen(JitouScreen.Home) },
                        onProposalChange = viewModel::setActiveProposal,
                        onHistoryAdd = viewModel::addAppointmentHistory,
                        onCompleteHaircut = { completedDate ->
                            viewModel.completeFriendHaircut(completedDate)
                            navigateToScreen(JitouScreen.Home)
                        },
                    )
                }

                JitouScreen.Profile -> {
                    ProfileRoute(
                        records = records,
                        nickname = uiState.nickname,
                        nicknameError = uiState.nicknameError,
                        isActive = screen == JitouScreen.Profile,
                        onBack = { navigateToScreen(JitouScreen.Home) },
                        onAddRecord = {
                            recordDialogPastDatesOnly = true
                            showRecordDialog = true
                        },
                        onReminderClick = { showReminderSheet = true },
                        onNicknameChange = viewModel::updateNickname,
                        onRefreshData = viewModel::refreshData,
                        isRefreshingData = uiState.isRefreshingData,
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        onLogout = {
                            navigateToScreen(JitouScreen.Home)
                            loginViewModel.signOut()
                        },
                    )
                }
            }
        }

        JitouBottomNav(
            selected = navSelectedScreen,
            pagePosition = navPosition,
            onSelect = navigateToScreen,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showRecordDialog) {
        RecordHaircutDialog(
            pastDatesOnly = recordDialogPastDatesOnly,
            onDismiss = {
                showRecordDialog = false
                recordDialogPastDatesOnly = false
            },
            onConfirm = { date ->
                viewModel.addHaircutRecord(date)
                showRecordDialog = false
                recordDialogPastDatesOnly = false
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
