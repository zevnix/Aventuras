package com.projectgame.app.data.local.entity

data class PetEvolutionEntity(
    val id: String,
    val petId: String,
    val stageName: String,
    val levelRequired: Int,
    val assetUrl: String
)
