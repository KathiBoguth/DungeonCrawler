package com.example.dungeoncrawler.entity

import com.example.dungeoncrawler.Settings
import kotlin.random.Random

class Level(
    chara: MainChara
) {

    val field: Array<Array<LevelObject?>> = Array(Settings.fieldSize) {
        (Array(Settings.fieldSize) {null})
    }

    lateinit var enemies: MutableList<BasicEnemy>

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
        val enemyList = ArrayList<BasicEnemy>()
        for (i in 0 until Settings.enemiesAmount) {
            var coordinates = getRandomCoordinates()
            while (field[coordinates.x][coordinates.y] != null) {
                coordinates = getRandomCoordinates()
            }
            val enemy = BasicEnemy("basicEnemy$i", field)
            field[coordinates.x][coordinates.y] = enemy
            enemy.position = coordinates
            enemy.direction = randomDirection()
            enemyList.add(enemy)
        }
        enemies = enemyList.toMutableList()

    }

    fun randomMoney(max: Int): Int {
        return random.nextInt(max)
    }

    private fun getRandomCoordinates(): Coordinates {
        val xCord = random.nextInt(field.size)
        val yCord = random.nextInt(field.size)

        return Coordinates(xCord, yCord)
    }

    private fun randomDirection(): Direction {
        return when (random.nextInt(4)){
            0 -> Direction.UP
            1 -> Direction.DOWN
            2 -> Direction.LEFT
            3 -> Direction.RIGHT
            else -> Direction.DOWN
        }
    }

}

class Coordinates(
    val x: Int,
    val y: Int
)