package com.example.dungeoncrawler

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import java.net.URI

class Settings {

    companion object {
        private const val roomsPath = "rooms"
        const val fieldSize = 12
        val field = listOf(
            listOf(URI("$roomsPath/room0.csv"), URI("$roomsPath/room1.csv")),
            listOf(URI("$roomsPath/room1.csv"), URI("$roomsPath/room1.csv"))
        )
        val startArea = Coordinates(5, 5)

        const val healthBaseValue = 100
        const val attackBaseValue = 10
        const val defenseBaseValue = 0
        const val moveLength = 80f
        const val treasureMax = 4
        const val margin = 16f
        const val treasureMaxMoney = 100
        const val nudgeWidth = 20f
        const val animDuration = 100L
        const val levelsMax = 3

        val enemiesPerLevel = mapOf(
            1 to arrayOf(EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.SLIME),
            2 to arrayOf(EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.WOLF),
            3 to arrayOf(EnemyEnum.WOLF, EnemyEnum.WOLF, EnemyEnum.WOLF),
            4 to arrayOf(EnemyEnum.OGRE)
        )
    }
}