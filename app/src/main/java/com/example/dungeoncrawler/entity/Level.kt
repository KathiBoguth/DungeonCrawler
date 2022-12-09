package com.example.dungeoncrawler.entity

import com.example.dungeoncrawler.Settings
import kotlin.random.Random

class Level(
    chara: MainChara,
    private val basicEnemy: BasicEnemy
) {

    val field: Array<Array<LevelObject?>> = Array(Settings.fieldSize) {
        (Array(Settings.fieldSize) {null})
    }

    private var random: Random = Random(System.currentTimeMillis())

    init {
        field[0][0] = chara
        placeTreasures()
        placeLadder()
        placeEnemies()

    }

    private fun placeTreasures() {
        val treasureCount = random.nextInt(1, Settings.treasureMax)

        for (i in 0..treasureCount) {

            var coordinates = getRandomCoordinates()
            while (field[coordinates.x][coordinates.y] != null) {
                coordinates = getRandomCoordinates()
            }

            val treasureId = "treasure$i"

            field[coordinates.x][coordinates.y] = Treasure(treasureId)
        }
    }

    private fun placeLadder() {
        var coordinates = getRandomCoordinates()
        while (field[coordinates.x][coordinates.y] != null) {
            coordinates = getRandomCoordinates()
        }
        field[coordinates.x][coordinates.y] = Ladder()

    }

    private fun placeEnemies() {
        var coordinates = getRandomCoordinates()
        while (field[coordinates.x][coordinates.y] != null) {
            coordinates = getRandomCoordinates()
        }
        field[coordinates.x][coordinates.y] = basicEnemy
        basicEnemy.position = coordinates
    }

    fun randomMoney(max: Int): Int {
        return random.nextInt(max)
    }

    private fun getRandomCoordinates(): Coordinates {
        val xCord = random.nextInt(field.size)
        val yCord = random.nextInt(field.size)

        return Coordinates(xCord, yCord)
    }


}

class Coordinates(
    val x: Int,
    val y: Int
)