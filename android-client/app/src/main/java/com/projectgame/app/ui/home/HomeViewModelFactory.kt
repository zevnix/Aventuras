package com.projectgame.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.projectgame.app.domain.repository.PlayerRepository

class HomeViewModelFactory(
    private val repository: PlayerRepository,
    private val childId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, childId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
