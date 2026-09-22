package com.projectgame.app.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class ChildAuthViewModelTest {

    // Testing state transitions without actual Supabase network calls
    // In a real environment we would use Mockk to mock SupabaseModule.client.auth

    @Test
    fun `login with empty credentials should remain Idle`() {
        val viewModel = ChildAuthViewModel()
        viewModel.login("", "")
        assertEquals(AuthState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `register with empty credentials should remain Idle`() {
        val viewModel = ChildAuthViewModel()
        viewModel.register("", "password")
        assertEquals(AuthState.Idle, viewModel.uiState.value)
    }
}
