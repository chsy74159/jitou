package com.jitou.app.ui.home

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jitou.app.data.local.JitouDatabase
import com.jitou.app.data.remote.SupabaseClientProvider
import com.jitou.app.data.remote.SupabaseRemoteDataSource
import com.jitou.app.data.repository.JitouRepository
import com.jitou.app.data.sync.SyncRepository
import com.jitou.app.model.AppointmentHistoryItem
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.HaircutRecord
import com.jitou.app.model.QueueEvent
import com.jitou.app.model.QueueState
import com.jitou.app.model.ReminderUiState
import com.jitou.app.model.fakeReminderState
import com.jitou.app.notifications.HaircutNotificationScheduler
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JitouHomeUiState(
    val records: List<HaircutRecord> = emptyList(),
    val proposal: HaircutProposal? = null,
    val reminder: ReminderUiState = fakeReminderState(),
    val appointmentHistory: List<AppointmentHistoryItem> = emptyList(),
    val isQueueing: Boolean = false,
    val friendName: String = "XX",
    val nickname: String = "Sion",
    val nicknameError: String? = null,
    val isRefreshingData: Boolean = false,
)

class JitouHomeViewModel(
    private val repository: JitouRepository,
    private val appContext: Context? = null,
) : ViewModel() {
    private val isQueueing = MutableStateFlow(false)
    private val friendName = MutableStateFlow("XX")
    private val profileState = MutableStateFlow(ProfileState())
    private var lastManualRefreshAtMillis = 0L

    private val contentState = combine(
        repository.haircutRecords,
        repository.activeProposal,
        repository.reminderState,
        repository.appointmentHistory,
        isQueueing,
    ) { records, proposal, reminder, appointmentHistory, isQueueing ->
        JitouHomeUiState(
            records = records,
            proposal = proposal,
            reminder = reminder,
            appointmentHistory = appointmentHistory,
            isQueueing = isQueueing,
        )
    }

    val uiState: StateFlow<JitouHomeUiState> = combine(
        contentState,
        friendName,
        profileState,
    ) { state, friendName, profile ->
        state.copy(
            friendName = friendName,
            nickname = profile.nickname,
            nicknameError = profile.errorMessage,
            isRefreshingData = profile.isRefreshingData,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = JitouHomeUiState(),
    )

    init {
        viewModelScope.launch {
            repository.syncAll()
            repository.profileNickname()?.let { nickname ->
                profileState.value = profileState.value.copy(nickname = nickname)
            }
            repository.friendName()?.let { name ->
                friendName.value = name
            }
            scheduleHaircutNotifications()
        }
    }

    fun addHaircutRecord(date: LocalDate) {
        viewModelScope.launch {
            repository.addHaircutRecord(date)
            scheduleHaircutNotifications()
            reduceQueue(QueueEvent.RecordedHaircut)
        }
    }

    fun completeFriendHaircut(date: LocalDate) {
        viewModelScope.launch {
            repository.addHaircutRecord(date, note = "和朋友一起")
            repository.setActiveProposal(null)
            scheduleHaircutNotifications()
            reduceQueue(QueueEvent.RecordedHaircut)
        }
    }

    fun setReminder(reminder: ReminderUiState) {
        viewModelScope.launch {
            repository.setReminder(reminder)
        }
    }

    fun setActiveProposal(proposal: HaircutProposal?) {
        viewModelScope.launch {
            repository.setActiveProposal(proposal)
            scheduleHaircutNotifications()
        }
    }

    fun addAppointmentHistory(item: AppointmentHistoryItem) {
        viewModelScope.launch {
            repository.addAppointmentHistory(item)
        }
    }

    fun updateNickname(nickname: String) {
        val trimmed = nickname.trim()
        if (trimmed.isBlank()) {
            profileState.update { it.copy(errorMessage = "昵称不能为空") }
            return
        }

        viewModelScope.launch {
            val savedNickname = repository.updateProfileNickname(trimmed)
            profileState.update { current ->
                if (savedNickname == null) {
                    current.copy(errorMessage = "昵称同步失败")
                } else {
                    current.copy(nickname = savedNickname, errorMessage = null)
                }
            }
        }
    }

    fun refreshData() {
        val now = SystemClock.elapsedRealtime()
        if (profileState.value.isRefreshingData || now - lastManualRefreshAtMillis < ManualRefreshCooldownMillis) {
            return
        }
        lastManualRefreshAtMillis = now

        viewModelScope.launch {
            profileState.update { it.copy(isRefreshingData = true) }
            try {
                repository.refreshRemoteChanges()
                repository.profileNickname()?.let { nickname ->
                    profileState.update { it.copy(nickname = nickname) }
                }
                repository.friendName()?.let { name ->
                    friendName.value = name
                }
                scheduleHaircutNotifications()
            } finally {
                val remainingCooldown = ManualRefreshCooldownMillis - (SystemClock.elapsedRealtime() - now)
                if (remainingCooldown > 0) {
                    delay(remainingCooldown)
                }
                profileState.update { it.copy(isRefreshingData = false) }
            }
        }
    }

    fun joinQueue() {
        reduceQueue(QueueEvent.JoinedQueue)
    }

    fun cancelQueue() {
        reduceQueue(QueueEvent.CancelledQueue)
    }

    private fun reduceQueue(event: QueueEvent) {
        isQueueing.update { current -> QueueState.reduce(current, event) }
    }

    private fun scheduleHaircutNotifications() {
        appContext?.let { HaircutNotificationScheduler.scheduleNext(it) }
    }

    private data class ProfileState(
        val nickname: String = "Sion",
        val errorMessage: String? = null,
        val isRefreshingData: Boolean = false,
    )

    companion object {
        private const val ManualRefreshCooldownMillis = 3_000L

        fun factory(context: Context): ViewModelProvider.Factory {
            val applicationContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(JitouHomeViewModel::class.java)) {
                        val database = JitouDatabase.getInstance(applicationContext)
                        val remote = SupabaseRemoteDataSource(SupabaseClientProvider.client)
                        val syncRepository = SyncRepository(database, remote)
                        return JitouHomeViewModel(
                            JitouRepository(
                                database = database,
                                syncRepository = syncRepository,
                            ),
                            appContext = applicationContext,
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
