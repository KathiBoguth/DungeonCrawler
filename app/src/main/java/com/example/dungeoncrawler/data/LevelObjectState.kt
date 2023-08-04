package com.example.dungeoncrawler.data

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.LevelObjectType

data class LevelObjectState(
    val id: String,
    val type: LevelObjectType,
    val position: Coordinates
)