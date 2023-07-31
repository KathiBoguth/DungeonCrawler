package com.example.dungeoncrawler.data

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.enemy.EnemyEnum

data class EnemyState(
    val id: String,
    val nudge: Boolean,
    val jump: Boolean,
    val direction: Direction,
    val position: Coordinates,
    val type: EnemyEnum,
    val flashRed: Boolean,
    val visible: Boolean
)