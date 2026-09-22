package com.projectgame.app.data.local.dao

import com.projectgame.app.data.local.entity.GameConfigEntity
import com.projectgame.app.data.local.entity.PetEvolutionEntity
import com.projectgame.app.data.local.entity.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

interface PlayerDao {
    fun getProfileFlow(childId: String): Flow<PlayerProfileEntity?>
    fun getEvolutionFlow(evolutionId: String): Flow<PetEvolutionEntity?>
    fun getGameConfigFlow(configKey: String): Flow<GameConfigEntity?>

    suspend fun insertProfile(profile: PlayerProfileEntity)
    suspend fun insertEvolutions(evolutions: List<PetEvolutionEntity>)
    suspend fun insertGameConfig(config: GameConfigEntity)
}
