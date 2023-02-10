package com.example.dungeoncrawler.entity.enemy

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction

data class EnemyPositionChangeDTO(
    val newPosition: Coordinates,
    val id: String
)

data class EnemyDamageDTO(
    val damage: Int,
    val direction: Direction,
    val id: String
)