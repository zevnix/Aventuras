package com.projectgame.app.domain.repository

import com.projectgame.app.data.local.dao.PlayerDao
import com.projectgame.app.data.local.entity.GameConfigEntity
import com.projectgame.app.data.local.entity.PetEvolutionEntity
import com.projectgame.app.data.local.entity.PlayerProfileEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import android.util.Log

// --- DTOs from Supabase ---
@Serializable
data class ProfileDto(
    val child_id: String,
    val level: Int,
    val current_pet_id: String? = null,
    val current_evolution_id: String? = null
)

@Serializable
data class ChildAuthDto(
    val id: String,
    val username: String,
    val display_name: String
)

@Serializable
data class BalanceDto(
    val child_id: String,
    val xp_balance: Int = 0,
    val coins_balance: Int = 0,
    val gems_balance: Int = 0
)

@Serializable
data class GameConfigDto(
    val key: String,
    val value: kotlinx.serialization.json.JsonElement
)

@Serializable
data class PetEvolutionDto(
    val id: String,
    val pet_id: String,
    val stage_name: String,
    val level_required: Int,
    val asset_url: String
)

@Serializable
data class MissionConfigDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val mission_type: String,
    val reward_xp: Int,
    val reward_coins: Int,
    val content: kotlinx.serialization.json.JsonElement? = null
)

@Serializable
data class MissionInstanceDto(
    val id: String,
    val child_id: String,
    val mission_config_id: String,
    val status: String
)

class PlayerRepository(
    private val dao: PlayerDao,
    private val supabase: SupabaseClient
) {
    // --- SSOT Readers (Flowing directly from Room) ---
    fun getProfileFlow(childId: String): Flow<PlayerProfileEntity?> = dao.getProfileFlow(childId)
    fun getEvolutionFlow(evolutionId: String): Flow<PetEvolutionEntity?> = dao.getEvolutionFlow(evolutionId)
    fun getGameConfigFlow(configKey: String): Flow<GameConfigEntity?> = dao.getGameConfigFlow(configKey)
    fun getAllMissionsFlow(): Flow<List<com.projectgame.app.data.local.entity.MissionConfigEntity>> = dao.getAllMissionsFlow()

    // --- Sync Strategy: Server-Authoritative Override ---
    suspend fun syncProfileData(childId: String) = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch Auth & Profile details
            val childResponse = supabase.postgrest["children"].select(columns = Columns.list("id, username, display_name")) {
                filter { eq("id", childId) }
            }.decodeSingle<ChildAuthDto>()

            val profileResponse = supabase.postgrest["player_profiles"].select(columns = Columns.list("child_id, level, current_pet_id, current_evolution_id")) {
                filter { eq("child_id", childId) }
            }.decodeSingle<ProfileDto>()

            // 2. Fetch Immutable Balance Projection
            val balanceResponse = supabase.postgrest["player_balances"].select() {
                filter { eq("child_id", childId) }
            }.decodeSingleOrNull<BalanceDto>() ?: BalanceDto(child_id = childId) // Fallback to 0 if no tx yet

            Log.d("PlayerRepository", "Sync successful! Balances: XP=${balanceResponse.xp_balance}, Coins=${balanceResponse.coins_balance}")

            // 3. Persist to Room (SSOT)
            val entity = PlayerProfileEntity(
                childId = childId,
                username = childResponse.username,
                displayName = childResponse.display_name,
                level = profileResponse.level,
                currentPetId = profileResponse.current_pet_id,
                currentEvolutionId = profileResponse.current_evolution_id,
                xpBalance = balanceResponse.xp_balance,
                coinsBalance = balanceResponse.coins_balance,
                gemsBalance = balanceResponse.gems_balance,
                lastSyncTime = System.currentTimeMillis()
            )
            dao.insertProfile(entity)

            // 4. Fetch the active evolution asset if any
            profileResponse.current_evolution_id?.let { evoId ->
                val evoResponse = supabase.postgrest["pet_evolutions"].select() {
                    filter { eq("id", evoId) }
                }.decodeSingleOrNull<PetEvolutionDto>()

                evoResponse?.let {
                    val evoEntity = PetEvolutionEntity(
                        id = it.id,
                        petId = it.pet_id,
                        stageName = it.stage_name,
                        levelRequired = it.level_required,
                        assetUrl = it.asset_url
                    )
                    dao.insertEvolutions(listOf(evoEntity))
                }
            }
        } catch(e: Exception) {
            Log.e("PlayerRepository", "Failed to sync profile data: ${e.message}", e)
            throw e
        }
    }

    suspend fun completeMission(childId: String, missionId: String) = withContext(Dispatchers.IO) {
        try {
            Log.d("PlayerRepository", "Attempting to complete mission $missionId for child $childId")
            // 1. We must first insert a mission_instance to satisfy the RPC requirements
            val createInstanceResponse = supabase.postgrest["mission_instances"].insert(
                mapOf(
                    "child_id" to childId,
                    "mission_config_id" to missionId,
                    "status" to "active"
                )
            ) {
                select()
            }.decodeSingle<MissionInstanceDto>()

            Log.d("PlayerRepository", "Instance created successfully: ${createInstanceResponse.id}")

            // 2. Call the server-authoritative completion RPC
            supabase.postgrest.rpc(
                "complete_digital_mission",
                mapOf("p_instance_id" to createInstanceResponse.id)
            )

            Log.d("PlayerRepository", "RPC executed successfully. Resyncing profile...")

            // 3. Resync profile to pull down the newly awarded XP/Coins
            syncProfileData(childId)
        } catch(e: Exception) {
            Log.e("PlayerRepository", "Failed to complete mission: ${e.message}", e)
            throw e
        }
    }

    suspend fun syncGameConfigs() = withContext(Dispatchers.IO) {
        // Fetch published game configs
        val configsResponse = supabase.postgrest["game_configs"].select() {
            filter { eq("status", "published") }
        }.decodeList<GameConfigDto>()

        val configEntities = configsResponse.map {
            GameConfigEntity(
                key = it.key,
                valueJson = it.value.toString()
            )
        }
        configEntities.forEach { dao.insertGameConfig(it) }

        // Fetch published missions
        val missionsResponse = supabase.postgrest["missions_config"].select() {
            filter { eq("status", "published") }
        }.decodeList<MissionConfigDto>()

        val missionEntities = missionsResponse.map {
            com.projectgame.app.data.local.entity.MissionConfigEntity(
                id = it.id,
                title = it.title,
                description = it.description,
                category = it.category,
                missionType = it.mission_type,
                rewardXp = it.reward_xp,
                rewardCoins = it.reward_coins,
                contentJson = it.content?.toString() ?: "{}"
            )
        }

        dao.insertMissionsConfig(missionEntities)
    }
}
