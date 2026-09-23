package com.projectgame.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.projectgame.app.data.local.entity.GameConfigEntity
import com.projectgame.app.data.local.entity.PetEvolutionEntity
import com.projectgame.app.data.local.entity.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_profile WHERE childId = :childId LIMIT 1")
    fun getProfileFlow(childId: String): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM pet_evolutions WHERE id = :evolutionId LIMIT 1")
    fun getEvolutionFlow(evolutionId: String): Flow<PetEvolutionEntity?>

    @Query("SELECT * FROM game_configs WHERE `key` = :configKey LIMIT 1")
    fun getGameConfigFlow(configKey: String): Flow<GameConfigEntity?>

    @Query("SELECT * FROM missions_config")
    fun getAllMissionsFlow(): Flow<List<com.projectgame.app.data.local.entity.MissionConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PlayerProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissionsConfig(missions: List<com.projectgame.app.data.local.entity.MissionConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvolutions(evolutions: List<PetEvolutionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameConfig(config: GameConfigEntity)
}
