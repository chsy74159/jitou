package com.jitou.app.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jitou.app.data.auth.AuthRepository
import com.jitou.app.data.local.JitouDatabase
import com.jitou.app.data.remote.SupabaseClientProvider
import com.jitou.app.data.remote.SupabaseRemoteDataSource
import com.jitou.app.data.sync.SyncRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isInitializing: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                _uiState.update {
                    it.copy(
                        isInitializing = status is SessionStatus.Initializing,
                        isAuthenticated = status is SessionStatus.Authenticated,
                    )
                }
                if (status is SessionStatus.Authenticated) {
                    runCatching { syncRepository.syncAll() }
                }
            }
        }
        viewModelScope.launch {
            runCatching { authRepository.restoreSession() }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isInitializing = false,
                            errorMessage = error.localizedMessage ?: "恢复登录状态失败",
                        )
                    }
                }
        }
    }

    fun signIn(account: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                authRepository.signIn(account, password)
                syncRepository.syncAll()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "登录失败",
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { authRepository.signOut() }
            _uiState.update { LoginUiState(isInitializing = false, isAuthenticated = false) }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                        val client = SupabaseClientProvider.client
                        val database = JitouDatabase.getInstance(appContext)
                        val remote = SupabaseRemoteDataSource(client)
                        return LoginViewModel(
                            authRepository = AuthRepository(client),
                            syncRepository = SyncRepository(database, remote),
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
