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
        const val diamondWorth = 300
        const val nudgeWidth = 20f
        const val animDuration = 100L
        const val bombCount = 5
        const val bombTimer = 3000L
        const val bombDamage = 40

        val enemiesPerLevel = mapOf(
            1 to arrayOf(EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.SLIME),
            2 to arrayOf(EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.PLANT),
            3 to arrayOf(
                EnemyEnum.SLIME,
                EnemyEnum.SLIME,
                EnemyEnum.SLIME,
                EnemyEnum.SLIME,
                EnemyEnum.PLANT,
                EnemyEnum.WOLF
            ),
            4 to arrayOf(
                EnemyEnum.SLIME,
                EnemyEnum.SLIME,
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.WOLF
            ),
            5 to arrayOf(
                EnemyEnum.SLIME,
                EnemyEnum.PLANT,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF
            ),
            6 to arrayOf(
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF
            ),
            7 to arrayOf(
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF
            ),
            8 to arrayOf(
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF
            ),
            9 to arrayOf(
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.PLANT,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF,
                EnemyEnum.WOLF
            ),
            10 to arrayOf(EnemyEnum.OGRE)
        )
    }
}