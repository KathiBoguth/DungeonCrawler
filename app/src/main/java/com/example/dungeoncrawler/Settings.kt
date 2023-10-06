package com.example.dungeoncrawler

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import java.net.URI

class Settings {

    companion object {
        private const val roomsPath = "rooms"
        val startRoomUri = URI("$roomsPath/roomWall.csv")
        val startCoordinates = Coordinates(3, 3)
        val endbossRoomUri = URI("$roomsPath/roomRandom.csv")
        val roomFiles = listOf(
            URI("$roomsPath/roomRandom.csv"),
            URI("$roomsPath/roomWall.csv"),
            URI("$roomsPath/roomWallBottom.csv"),
            URI("$roomsPath/roomWallTop.csv"),
            URI("$roomsPath/roomWallLeft.csv"),
            URI("$roomsPath/roomWallRight.csv")
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

        val enemiesPerLevel = mapOf(
            1 to arrayOf(EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.SLIME),
            2 to arrayOf(EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.PLANT),
            3 to arrayOf(EnemyEnum.WOLF, EnemyEnum.WOLF, EnemyEnum.PLANT, EnemyEnum.PLANT),
            4 to arrayOf(EnemyEnum.WOLF, EnemyEnum.WOLF, EnemyEnum.WOLF, EnemyEnum.PLANT),
            5 to arrayOf(EnemyEnum.OGRE)
        )
    }
}