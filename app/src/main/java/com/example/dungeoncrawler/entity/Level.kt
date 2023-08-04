package com.example.dungeoncrawler.entity

import androidx.compose.ui.unit.Dp
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.armor.Cuirass
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import com.example.dungeoncrawler.entity.enemy.Ogre
import com.example.dungeoncrawler.entity.enemy.Slime
import com.example.dungeoncrawler.entity.enemy.Wolf
import com.example.dungeoncrawler.entity.weapon.Arrow
import com.example.dungeoncrawler.entity.weapon.Bow
import com.example.dungeoncrawler.entity.weapon.Sword
import com.example.dungeoncrawler.entity.weapon.Weapon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

class Level(
    private var chara: MainChara
) {

    companion object {
        const val SWORD_WOODEN = "sword_wooden"
        const val SWORD_DIAMOND = "sword_diamond"
        const val CUIRASS_RAG = "cuirass_rag"
        const val CUIRASS_IRON = "cuirass_iron"
        const val CUIRASS_DIAMOND = "cuirass_diamond"
        const val BOW_WOODEN = "bow_wooden"
        const val ARROW = "arrow"
        private const val LADDER = "ladder"
        private const val TREASURE = "treasure"
    }

    var field: Array<Array<MutableList<LevelObject>>> = Array(Settings.fieldSize) {
        (Array(Settings.fieldSize) { mutableListOf() })
    }

    val coinStack = ArrayDeque<String>()
    val potionStack = ArrayDeque<String>()
    private lateinit var weapons: List<Weapon>
    private lateinit var armors: List<Armor>
    val gameObjectIds: MutableList<String> = mutableListOf()
    val movableEntitiesList: MutableList<MovableEntity> = mutableListOf()
    val enemyPositionFlow = MutableStateFlow(LevelObjectPositionChangeDTO(Coordinates(-1,-1), Direction.DOWN,""))

    private var random: Random = Random(System.currentTimeMillis())
    var levelCount = 1

    init {
        initLevel()
    }

    fun initLevel() {
        gameObjectIds.clear()
        gameObjectIds.addAll(
            listOf(
                SWORD_WOODEN,
                SWORD_DIAMOND,
                CUIRASS_RAG,
                CUIRASS_IRON,
                CUIRASS_DIAMOND,
                BOW_WOODEN,
                "${ARROW}_left", "${ARROW}_right", "${ARROW}_up", "${ARROW}_down",
                LADDER,
                "${TREASURE}0", "${TREASURE}1", "${TREASURE}2", "${TREASURE}3"
            )
        )
        field = Array(Settings.fieldSize) {
            (Array(Settings.fieldSize) { mutableListOf() })
        }
        weapons = listOf(Sword(10, SWORD_WOODEN), Sword(50, SWORD_DIAMOND), Bow(10, BOW_WOODEN))
        armors = listOf(
            Cuirass(10, CUIRASS_RAG),
            Cuirass(25, CUIRASS_IRON),
            Cuirass(50, CUIRASS_DIAMOND)
        )
        placeWalls()
        val endBoss = levelCount >= Settings.enemiesPerLevel.size
        if (!endBoss) {
            placeTreasures()
            placeLadder()
        }
        placeEnemies()
        fillCoinStack()
        fillPotionStack()
        val randomStartCoordinates = randomFreeCoordinates()
        field[randomStartCoordinates.x][randomStartCoordinates.y].add(chara) // TODO: should chara be on field or in movableEntities list?
    }

    private fun placeWalls() {
        for (row in field.indices) {
            for (column in field[row].indices) {
                if (row == 0 || column == 0 || row == field.size - 1 || column == field[row].size - 1) {
                    field[row][column].add(Wall())
                }
            }
        }
    }

    private fun placeTreasures() {
        val treasureCount = random.nextInt(1, Settings.treasureMax)

        for (i in 0..treasureCount) {

            val coordinates = randomFreeCoordinates()
            val treasureId = "treasure$i"
            placeTreasure(coordinates, treasureId)

        }
    }

    private fun placeTreasure(coordinates: Coordinates, treasureId: String) {
        field[coordinates.x][coordinates.y].add(Treasure(treasureId))
        gameObjectIds.add(treasureId)
    }

    private fun placeLadder(coordinates: Coordinates? = null) {
        if (coordinates != null) {
            field[coordinates.x][coordinates.y].add(Ladder())
            return
        }
        val randomCoordinates = randomFreeCoordinates()
        field[randomCoordinates.x][randomCoordinates.y].add(Ladder())

    }

    private fun placeEnemies() {
        val enemyList = ArrayList<BasicEnemy>()
        movableEntitiesList.clear()
        Settings.enemiesPerLevel[levelCount]?.forEach { enemyType ->
            val coordinates = randomFreeCoordinates() // TODO: should enemies be placed on non-steppable stuff as well?
            val enemy = when (enemyType) {
                EnemyEnum.SLIME -> {
                    val count =
                        enemyList.count { alreadyAdded -> alreadyAdded.id.contains("slime") }
                    Slime("slime$count", enemyPositionFlow)
                }

                EnemyEnum.WOLF -> {
                    val count = enemyList.count { alreadyAdded -> alreadyAdded.id.contains("wolf") }
                    Wolf("wolf$count", enemyPositionFlow)
                }

                EnemyEnum.OGRE -> {
                    Ogre("ogre", enemyPositionFlow)
                }

            }
            setMoveRunnable(enemy)

            // field[coordinates.x][coordinates.y].add(enemy)
            enemy.position = coordinates
            enemy.direction = randomDirection()
            enemyList.add(enemy)
            gameObjectIds.add(enemy.id)
            movableEntitiesList.add(enemy)
        }

    }

    private fun setMoveRunnable(enemy: BasicEnemy) {
        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                enemy.move(field)
                enemy.handler.postDelayed(this, enemy.speed.toLong())
            }
        }
        enemy.handler.postDelayed(runnableCode, enemy.speed.toLong())
    }

    private fun fillCoinStack() {
        val coinIds = listOf("coin0", "coin1", "coin2")
        coinIds.forEach {
            coinStack.addLast(it)
        }
        gameObjectIds.addAll(coinIds)
    }

    private fun fillPotionStack() {
        val potionIds = listOf("potion0", "potion1", "potion2")
        potionIds.forEach {
            potionStack.addLast(it)
        }
        gameObjectIds.addAll(potionIds)
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
        return when (random.nextInt(4)) {
            0 -> Direction.UP
            1 -> Direction.DOWN
            2 -> Direction.LEFT
            3 -> Direction.RIGHT
            else -> Direction.DOWN
        }
    }

    fun drop(): LevelObjectType {

        val randomValue = random.nextFloat()
        return if (randomValue < 0.35) {
            LevelObjectType.COIN
        } else if (randomValue < 0.6) {
            LevelObjectType.POTION
        } else if (randomValue < 0.85) {
            LevelObjectType.ARMOR
        } else {
            LevelObjectType.WEAPON
        }

    }

    fun randomWeapon(): Weapon {
        val randomValue = random.nextFloat()
        // TODO: probably display issues?

        return if (randomValue < 0.4) {
            weapons.find { it.id == BOW_WOODEN } ?: weapons.first()
        } else if (randomValue < 0.8) {
            weapons.find { it.id == SWORD_WOODEN } ?: weapons.first()
        } else {
            weapons.find { it.id == SWORD_DIAMOND } ?: weapons.first()
        }
    }

    fun randomArmor(): Armor {
        val randomValue = random.nextFloat()

        if (randomValue < 0.5) {
            return armors.find { it.id == CUIRASS_RAG } ?: armors.first()
        } else if (randomValue < 0.8) {
            return armors.find { it.id == CUIRASS_IRON } ?: armors.first()
        }
        return armors.find { it.id == CUIRASS_DIAMOND } ?: armors.first()
    }

    private fun randomFreeCoordinates(): Coordinates {
        var randomCoordinates = getRandomCoordinates()
        while (field[randomCoordinates.x][randomCoordinates.y].isNotEmpty()
            || movableEntitiesList.any { it.position == randomCoordinates }
        ) {
            randomCoordinates = getRandomCoordinates()
        }
        return randomCoordinates
    }

    fun throwArrow(
        coordinates: Coordinates,
        direction: Direction,
        attack: (enemy: BasicEnemy) -> Unit
    ) {

        val id = when (direction) {
            Direction.UP -> "${ARROW}_up"
            Direction.LEFT -> "${ARROW}_left"
            Direction.DOWN -> "${ARROW}_down"
            Direction.RIGHT -> "${ARROW}_right"
        }
        val arrow = Arrow(id, direction, coordinates)

        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                val enemy = arrow.move(field, movableEntitiesList)
                if (enemy != null) {
                    attack(enemy)
                }
                if (arrow.isActive) {
                    arrow.handler.postDelayed(this, arrow.speed.toLong())
                }
            }
        }
        arrow.handler.postDelayed(runnableCode, arrow.speed.toLong())
    }

    fun endBossDefeated() {
        val xCoord = (field.size/2)
        val yCoord = (field[xCoord].size/2)
        val coordinatesLadder = Coordinates(xCoord - 1, yCoord)
        placeLadder(coordinatesLadder)

        val coordinatesTreasure = Coordinates(xCoord + 1, yCoord)
        placeTreasure(coordinatesTreasure, "treasure0")
    }

}

data class Coordinates(
    val x: Int,
    val y: Int
)
data class CoordinatesDp(
    val x: Dp,
    val y: Dp
)