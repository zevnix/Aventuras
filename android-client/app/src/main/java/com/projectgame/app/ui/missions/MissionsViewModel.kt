package com.projectgame.app.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectgame.app.data.local.entity.MissionConfigEntity
import com.projectgame.app.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MissionsUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val missions: List<MissionConfigEntity> = emptyList()
)

class MissionsViewModel(
    private val repository: PlayerRepository,
    private val childId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(MissionsUiState())
    val uiState: StateFlow<MissionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllMissionsFlow().collect { missionList ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        missions = missionList
                    )
                }
            }
        }
        syncMissions()
    }

    fun syncMissions() {
        _uiState.update { it.copy(isLoading = true, isOffline = false) }
        viewModelScope.launch {
            try {
                repository.syncGameConfigs() // This now fetches missions_config as well
                _uiState.update { it.copy(isLoading = false, isOffline = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isOffline = true) }
            }
        }
    }

    fun completeDigitalMission(missionConfigId: String) {
        viewModelScope.launch {
            try {
                repository.completeMission(childId, missionConfigId)
            } catch (e: Exception) {
                // Ignore for now in vertical slice
            }
        }
    }
}
