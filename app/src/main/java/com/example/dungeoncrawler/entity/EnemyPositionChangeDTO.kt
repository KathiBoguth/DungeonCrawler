package com.example.dungeoncrawler.entity

data class EnemyPositionChangeDTO(
    val newPosition: Coordinates,
    val id: String
)

data class EnemyDamageDTO(
    val damage: Int,
    val id: String
)