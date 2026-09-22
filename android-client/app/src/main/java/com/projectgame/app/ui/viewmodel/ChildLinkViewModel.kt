package com.projectgame.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectgame.app.data.supabase.SupabaseModule
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ConfirmLinkRequest(val p_code: String)

class ChildLinkViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LinkState>(LinkState.Idle)
    val uiState: StateFlow<LinkState> = _uiState

    fun confirmLinkCode(code: String) {
        if (code.isBlank()) return

        _uiState.value = LinkState.Loading

        viewModelScope.launch {
            try {
                // Calling the RPC we established in 0005_audit_fixes_and_security_enhancements.sql
                val response = SupabaseModule.client.postgrest.rpc(
                    "confirm_link_request",
                    ConfirmLinkRequest(code)
                )

                // If it doesn't throw, it was successful according to our RPC implementation
                _uiState.value = LinkState.Success
            } catch (e: Exception) {
                _uiState.value = LinkState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

sealed class LinkState {
    object Idle : LinkState()
    object Loading : LinkState()
    object Success : LinkState()
    data class Error(val message: String) : LinkState()
}
