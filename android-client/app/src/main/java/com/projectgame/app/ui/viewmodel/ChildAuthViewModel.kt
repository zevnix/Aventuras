package com.projectgame.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectgame.app.data.supabase.SupabaseModule
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChildAuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState

    fun login(emailParam: String, passwordParam: String) {
        if (emailParam.isBlank() || passwordParam.isBlank()) return

        _uiState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                SupabaseModule.client.auth.signInWith(Email) {
                    email = emailParam
                    password = passwordParam
                }
                _uiState.value = AuthState.Success
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(emailParam: String, passwordParam: String) {
        if (emailParam.isBlank() || passwordParam.isBlank()) return

        _uiState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                SupabaseModule.client.auth.signUpWith(Email) {
                    email = emailParam
                    password = passwordParam
                    // Pass metadata to trigger so it correctly maps to public.children
                    data = kotlinx.serialization.json.buildJsonObject {
                        put("role", kotlinx.serialization.json.JsonPrimitive("child"))
                        val generatedUsername = "Player" + (1000..9999).random().toString()
                        put("username", kotlinx.serialization.json.JsonPrimitive(generatedUsername))
                        put("display_name", kotlinx.serialization.json.JsonPrimitive(generatedUsername))
                    }
                }
                _uiState.value = AuthState.Success
            } catch (e: Exception) {
                _uiState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
