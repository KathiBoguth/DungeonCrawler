package com.example.dungeoncrawler

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction

data class CharaState(
    val nudge: Boolean,
    val direction: Direction,
    val position: Coordinates
)