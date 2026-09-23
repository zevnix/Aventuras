package com.projectgame.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions_config")
data class MissionConfigEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val missionType: String,
    val rewardXp: Int,
    val rewardCoins: Int,
    val contentJson: String
)
