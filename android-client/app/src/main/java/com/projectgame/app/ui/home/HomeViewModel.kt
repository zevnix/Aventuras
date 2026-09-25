package com.projectgame.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectgame.app.data.local.entity.PlayerProfileEntity
import com.projectgame.app.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LevelThreshold(val level: Int, val xp_required: Int)

@Serializable
data class ThemeConfig(
    val parallax_sky_top: String = "#81D4FA",
    val parallax_sky_bottom: String = "#B3E5FC",
    val parallax_ground_top: String = "#4CAF50",
    val parallax_ground_bottom: String = "#1B5E20",
    val board_color: String = "#5D4037",
    val board_accent: String = "#8D6E63",
    val button_primary: String = "#43A047",
    val button_primary_accent: String = "#A5D6A7"
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val profile: PlayerProfileEntity? = null,
    val currentAssetUrl: String? = null,
    val visualProgressPercentage: Float = 0f,
    val targetXpForNextLevel: Int = 0,
    val themeConfig: ThemeConfig = ThemeConfig()
)

class HomeViewModel(
    private val repository: PlayerRepository,
    private val childId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val jsonParser = Json { ignoreUnknownKeys = true }

    init {
        // 1. Instantly subscribe to the Local Source of Truth
        viewModelScope.launch {
            combine(
                repository.getProfileFlow(childId),
                repository.getGameConfigFlow("level_thresholds"),
                repository.getGameConfigFlow("active_theme")
            ) { profile, levelConfig, themeConfigEntity ->
                Triple(profile, levelConfig, themeConfigEntity)
            }.collect { (profile, levelConfig, themeConfigEntity) ->
                val parsedTheme = themeConfigEntity?.valueJson?.let {
                    try { jsonParser.decodeFromString<ThemeConfig>(it) } catch(e: Exception) { ThemeConfig() }
                } ?: ThemeConfig()

                if (profile != null) {
                    val thresholds = levelConfig?.valueJson?.let { parseThresholds(it) } ?: emptyList()
                    val (progress, nextXp) = calculateVisualProgress(profile.level, profile.xpBalance, thresholds)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = profile,
                            visualProgressPercentage = progress,
                            targetXpForNextLevel = nextXp,
                            themeConfig = parsedTheme
                        )
                    }

                    // Also fetch asset url if evolution changed
                    profile.currentEvolutionId?.let { evoId ->
                        repository.getEvolutionFlow(evoId).firstOrNull()?.let { evo ->
                            _uiState.update { state -> state.copy(currentAssetUrl = evo.assetUrl) }
                        }
                    }
                }
            }
        }

        // 2. Trigger async refresh (Network Sync)
        syncData()
    }

    fun syncData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, isOffline = false) }
        viewModelScope.launch {
            try {
                repository.syncGameConfigs()
                repository.syncProfileData(childId)
                _uiState.update { it.copy(isLoading = false, isOffline = false) }
            } catch (e: Exception) {
                // If network fails, we rely on Room data and mark offline state.
                _uiState.update { it.copy(isLoading = false, isOffline = true) }
            }
        }
    }

    private fun parseThresholds(jsonString: String): List<LevelThreshold> {
        return try {
            jsonParser.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * The Level is Authoritative from the DB profile.
     * We ONLY use thresholds to render the visual % filling of the bar.
     * If the client has enough XP to level up but the server hasn't updated the level,
     * the bar stays at 100% full, but the level label does NOT change.
     */
    fun calculateVisualProgress(
        serverLevel: Int,
        currentXp: Int,
        thresholds: List<LevelThreshold>
    ): Pair<Float, Int> {
        if (thresholds.isEmpty()) return Pair(0f, 0)

        // Sort by level just in case
        val sorted = thresholds.sortedBy { it.level }

        // Find thresholds for current level and next level
        val currentThresh = sorted.find { it.level == serverLevel }?.xp_required ?: 0
        val nextThresh = sorted.find { it.level == serverLevel + 1 }?.xp_required ?: currentThresh

        // Max Level Reached
        if (nextThresh == currentThresh) return Pair(1f, currentThresh)

        // Prevent calculating progress for previous levels
        val xpInCurrentLevel = currentXp - currentThresh
        val totalXpRequiredForNext = nextThresh - currentThresh

        if (xpInCurrentLevel < 0) return Pair(0f, nextThresh)

        // Server hasn't processed level up yet
        if (xpInCurrentLevel >= totalXpRequiredForNext) return Pair(1f, nextThresh)

        val percentage = xpInCurrentLevel.toFloat() / totalXpRequiredForNext.toFloat()
        return Pair(percentage, nextThresh)
    }
}
