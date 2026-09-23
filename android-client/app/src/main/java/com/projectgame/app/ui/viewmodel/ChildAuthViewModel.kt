package com.projectgame.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectgame.app.data.supabase.SupabaseModule
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChildAuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val uiState: StateFlow<AuthState> = _uiState

    init {
        checkOrCreateSession()
    }

    fun checkOrCreateSession() {
        _uiState.value = AuthState.Initializing

        viewModelScope.launch {
            try {
                // 1. Wait for Supabase to restore the session from DataStore
                SupabaseModule.client.auth.awaitInitialization()

                // 2. Check if a session already exists
                val currentSession = SupabaseModule.client.auth.currentSessionOrNull()

                if (currentSession != null) {
                    _uiState.value = AuthState.Success(currentSession.user?.id ?: "")
                } else {
                    // 3. No session found. Initiate Anonymous Sign-In
                    _uiState.value = AuthState.CreatingAnonymousSession
                    SupabaseModule.client.auth.signInAnonymously()

                    val newSession = SupabaseModule.client.auth.currentSessionOrNull()
                    if (newSession != null) {
                        _uiState.value = AuthState.Success(newSession.user?.id ?: "")
                    } else {
                        _uiState.value = AuthState.Error("Fallo al crear cuenta anónima.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Error de red al conectar.")
            }
        }
    }
}

sealed class AuthState {
    object Initializing : AuthState()
    object CreatingAnonymousSession : AuthState()
    data class Success(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
