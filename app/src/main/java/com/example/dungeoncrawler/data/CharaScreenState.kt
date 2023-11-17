package com.example.dungeoncrawler.data

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction

data class CharaScreenState(
    val nudge: Boolean,
    val jump: Boolean,
    val direction: Direction,
    val position: Coordinates,
    val flashRed: Boolean,
    val health: Int,
    val gold: Int,
    val weaponId: String,
    val cuirassId: String,
    val fixated: Boolean,
    val bombAmount: Int
)