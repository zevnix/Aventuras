package com.projectgame.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_evolutions")
data class PetEvolutionEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val stageName: String,
    val levelRequired: Int,
    val assetUrl: String
)
