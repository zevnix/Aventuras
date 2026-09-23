package com.projectgame.app.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.projectgame.app.domain.repository.PlayerRepository

class MissionsViewModelFactory(
    private val repository: PlayerRepository,
    private val childId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MissionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MissionsViewModel(repository, childId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
