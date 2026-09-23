package com.projectgame.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_configs")
data class GameConfigEntity(
    @PrimaryKey val key: String,
    val valueJson: String
)
