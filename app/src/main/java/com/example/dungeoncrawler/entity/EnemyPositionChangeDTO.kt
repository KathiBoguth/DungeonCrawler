package com.example.dungeoncrawler.entity

data class EnemyPositionChangeDTO(
    val newPosition: Coordinates,
    val id: String
)

data class EnemyDamageDTO(
    val damage: Int,
    val direction: Direction,
    val id: String
)