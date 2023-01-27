package com.example.dungeoncrawler.entity

import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.entity.weapon.Sword
import com.example.dungeoncrawler.entity.weapon.Weapon
import kotlin.random.Random

class Level(
    chara: MainChara
) {

    val field: Array<Array<MutableList<LevelObject>>> = Array(Settings.fieldSize) {
        (Array(Settings.fieldSize) { mutableListOf() })
    }

    lateinit var enemies: MutableList<BasicEnemy>
    val coinStack = ArrayDeque<String>()
    val swordIds = listOf("sword_wooden", "sword_diamond")
    val levelCount = 1

    private var random: Random = Random(System.currentTimeMillis())

    init {
        field[1][1].add(chara)
        placeWalls()
        placeTreasures()
        placeLadder()
        placeEnemies()
        fillCoinStack()

    }

    private fun placeWalls() {
        for (row in field.indices) {
            for (column in field[row].indices) {
                if (row == 0 || column == 0 || row == field.size-1 || column == field[row].size-1){
                    field[row][column].add(Wall())
                }
            }
        }
    }

    private fun placeTreasures() {
        val treasureCount = random.nextInt(1, Settings.treasureMax)

        for (i in 0..treasureCount) {

            var coordinates = getRandomCoordinates()
            while (field[coordinates.x][coordinates.y].isNotEmpty()) {
                coordinates = getRandomCoordinates()
            }

            val treasureId = "treasure$i"

            field[coordinates.x][coordinates.y].add(Treasure(treasureId))
        }
    }

    private fun placeLadder() {
        var coordinates = getRandomCoordinates()
        while (field[coordinates.x][coordinates.y].isNotEmpty()) {
            coordinates = getRandomCoordinates()
        }
        field[coordinates.x][coordinates.y].add(Ladder())

    }

    private fun placeEnemies() {
        val enemyList = ArrayList<BasicEnemy>()
        for (i in 0 until Settings.enemiesAmount) {
            var coordinates = getRandomCoordinates()
            var levelObjectsList = field[coordinates.x][coordinates.y]
            while (levelObjectsList.isNotEmpty() || levelObjectsList.any { it !is CanStandOn }) {
                coordinates = getRandomCoordinates()
                levelObjectsList = field[coordinates.x][coordinates.y]
            }
            val enemy = BasicEnemy("basicEnemy$i", field)
            field[coordinates.x][coordinates.y].add(enemy)
            enemy.position = coordinates
            enemy.direction = randomDirection()
            enemyList.add(enemy)
        }
        enemies = enemyList.toMutableList()

    }

    private fun fillCoinStack() {
        coinStack.addLast("coin0")
        coinStack.addLast("coin1")
        coinStack.addLast("coin2")
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

    fun dropCoin(): Boolean {
        return random.nextBoolean()

    }

    fun randomWeapon(): Weapon {
        return if (random.nextInt() > 0.8) {
            Sword(50, "sword_diamond")
        } else {
            Sword(10, "sword_wooden")
        }

    }

}

class Coordinates(
    val x: Int,
    val y: Int
)