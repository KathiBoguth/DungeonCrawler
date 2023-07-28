package com.example.dungeoncrawler.data

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction

data class CharaState(
    val nudge: Boolean,
    val jump: Boolean,
    val direction: Direction,
    val position: Coordinates,
    val flashRed: Boolean,
    val health: Int,
    val gold: Int
)