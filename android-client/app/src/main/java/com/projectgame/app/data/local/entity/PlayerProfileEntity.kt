package com.projectgame.app.data.local.entity

data class PlayerProfileEntity(
    val childId: String,
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
