package com.projectgame.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val childId: String,
    val username: String,
    val displayName: String,
    val level: Int,
    val currentPetId: String?,
    val currentEvolutionId: String?,
    val xpBalance: Int,
    val coinsBalance: Int,
    val gemsBalance: Int,
    val lastSyncTime: Long
)
