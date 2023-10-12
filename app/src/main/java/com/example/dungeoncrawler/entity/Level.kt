package com.example.dungeoncrawler.entity

import android.content.Context
import android.util.Log
import androidx.compose.ui.unit.Dp
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.armor.Cuirass
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import com.example.dungeoncrawler.entity.enemy.Ogre
import com.example.dungeoncrawler.entity.enemy.Plant
import com.example.dungeoncrawler.entity.enemy.Slime
import com.example.dungeoncrawler.entity.enemy.Wolf
import com.example.dungeoncrawler.entity.weapon.Arrow
import com.example.dungeoncrawler.entity.weapon.Bow
import com.example.dungeoncrawler.entity.weapon.Pebble
import com.example.dungeoncrawler.entity.weapon.Sword
import com.example.dungeoncrawler.entity.weapon.Weapon
import com.example.dungeoncrawler.service.FileReaderService
import com.example.dungeoncrawler.service.PathFindingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.URI
import kotlin.random.Random

class Level(
    var chara: MainChara
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

    val fileReaderService = FileReaderService()

    var field: List<List<MutableList<LevelObject>>> = emptyList()
    var fieldLayout: List<List<GroundType>> = emptyList()

    val coinStack = ArrayDeque<String>()
    val potionStack = ArrayDeque<String>()
    private lateinit var weapons: List<Weapon>
    private lateinit var armors: List<Armor>
    val gameObjectIds: MutableList<String> = mutableListOf()
    val movableEntitiesList: MutableList<MovableEntity> = mutableListOf()
    val enemyPositionFlow = MutableStateFlow(
        LevelObjectPositionChangeDTO(
            Coordinates(-1, -1),
            Direction.DOWN,
            false,
            ""
        )
    )

    var charaFixated = false

    var arrowFlow: StateFlow<LevelObjectPositionChangeDTO>? = null
    var pebbleFlow: StateFlow<LevelObjectPositionChangeDTO>? = null

    private var random: Random = Random(System.currentTimeMillis())
    var levelCount = 1

    fun initLevel(context: Context) {
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
        for (i in 0..10) {
            val successful = initField(context)
            if (successful) {
                return
            }
        }
        Log.e("Level", "level creation failed, try recursively again")
        initLevel(context)

    }

    private fun initField(context: Context): Boolean {
        val endBoss = levelCount >= Settings.enemiesPerLevel.size

        val fieldScheme = if (endBoss) {
            listOf(listOf(Settings.endbossRoomUri))
        } else {
            getRandomFieldLayout()
        }
        fieldLayout = fileReaderService.parseFieldSchemeToField(fieldScheme, context)
        field = fieldLayout.map {
            it.map { groundType ->
                when (groundType) {
                    GroundType.STONE -> mutableListOf(Wall())
                    else -> mutableListOf()
                }
            }
        }

        weapons = listOf(Sword(10, SWORD_WOODEN), Sword(50, SWORD_DIAMOND), Bow(10, BOW_WOODEN))
        armors = listOf(
            Cuirass(10, CUIRASS_RAG),
            Cuirass(25, CUIRASS_IRON),
            Cuirass(50, CUIRASS_DIAMOND)
        )
        placeWalls()
        if (!endBoss) {
            placeTreasures()
            val ladderPlaced = placeLadder()
            if (!ladderPlaced) {
                return false
            }
        }
        placeEnemies()
        movableEntitiesList.add(chara)
        fillCoinStack()
        fillPotionStack()
        field[Settings.startCoordinates.x][Settings.startCoordinates.y].add(chara)
        chara.position = Settings.startCoordinates
        return true
    }

    private fun getRandomFieldLayout(): List<List<URI>> {
        val size = random.nextInt(4) + 1
        val fieldLayout = mutableListOf<List<URI>>()
        for (i in 0..size) {
            val list = mutableListOf<URI>()
            for (j in 0..size) {
                val file = if (i == 0 && j == 0) {
                    Settings.startRoomUri
                } else {
                    Settings.roomFiles[random.nextInt(Settings.roomFiles.size)]
                }
                list.add(file)
            }
            fieldLayout.add(list)
        }
        return fieldLayout
    }

    private fun placeWalls() {
        // TODO: needed?
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

            val coordinates = randomFreeCoordinates(excluding = Settings.startArea)
            val treasureId = "treasure$i"
            placeTreasure(coordinates, treasureId)

        }
    }

    private fun placeTreasure(coordinates: Coordinates, treasureId: String) {
        field[coordinates.x][coordinates.y].add(Treasure(treasureId))
        gameObjectIds.add(treasureId)
    }

    private fun placeLadder(coordinates: Coordinates? = null): Boolean {
        if (coordinates != null) {
            field[coordinates.x][coordinates.y].add(Ladder())
            return true
        }
        val fieldIntArray = PathFindingService.fieldToIntArray(fieldLayout)
        var randomCoordinates = randomFreeCoordinates(excluding = Settings.startArea)
        var isPathToLadder =
            PathFindingService.isPath(fieldIntArray, Settings.startCoordinates, randomCoordinates)
        for (i in 0..10) {
            if (isPathToLadder) {
                field[randomCoordinates.x][randomCoordinates.y].add(Ladder())
                return true
            } else {
                randomCoordinates = randomFreeCoordinates(excluding = Settings.startArea)
                isPathToLadder = PathFindingService.isPath(
                    fieldIntArray,
                    Settings.startCoordinates,
                    randomCoordinates
                )
            }
        }
        return false


    }

    private fun placeEnemies() {
        val enemyList = ArrayList<BasicEnemy>()
        movableEntitiesList.filterIsInstance<BasicEnemy>().forEach {
            it.destroy()
        }
        movableEntitiesList.clear()
        Settings.enemiesPerLevel[levelCount]?.forEach { enemyType ->
            val coordinates =
                randomFreeCoordinates(excluding = Settings.startArea) // TODO: should enemies be placed on non-steppable stuff as well?
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

                EnemyEnum.PLANT -> {
                    val count =
                        enemyList.count { alreadyAdded -> alreadyAdded.id.contains("plant") }
                    Plant("plant$count", enemyPositionFlow)
                }

                EnemyEnum.OGRE -> {
                    Ogre("ogre", enemyPositionFlow)
                }

            }
            setMoveRunnable(enemy)

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

    private fun getRandomCoordinates(
        bounds: Coordinates = Coordinates(
            field.size,
            field[0].size
        )
    ): Coordinates {
        val xCord = random.nextInt(bounds.x)
        val yCord = random.nextInt(bounds.y)

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

    private fun randomFreeCoordinates(
        including: Coordinates = Coordinates(field.size, field[0].size),
        excluding: Coordinates = Coordinates(0, 0)
    ): Coordinates {
        var randomCoordinates = getRandomCoordinates(bounds = including)
        while (randomCoordinates.x < excluding.x && randomCoordinates.y < excluding.y
            || field[randomCoordinates.x][randomCoordinates.y].isNotEmpty()
            || movableEntitiesList.any { it.position == randomCoordinates }
        ) {
            randomCoordinates = getRandomCoordinates(bounds = including)
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

        gameObjectIds.add(id)
        field[coordinates.x][coordinates.y].add(arrow)
        arrowFlow = arrow.positionFlow

        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                val enemy = arrow.move(field, movableEntitiesList)
                if (enemy != null && enemy.type == LevelObjectType.ENEMY) {
                    attack(enemy as BasicEnemy)
                }
                if (arrow.isActive) {
                    arrow.handler.postDelayed(this, arrow.speed.toLong())
                }
            }
        }
        arrow.handler.postDelayed(runnableCode, arrow.speed.toLong())
    }

    fun throwPebble(
        coordinates: Coordinates,
        direction: Direction,
        attack: (attackalue: Int) -> Unit
    ) {
        val id = "pebble_throwable"
        val pebble = Pebble(direction, coordinates)

        gameObjectIds.add(id)
        field[coordinates.x][coordinates.y].add(pebble)
        pebbleFlow = pebble.positionFlow

        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                val chara = pebble.move(field, movableEntitiesList)
                if (chara != null && chara.type == LevelObjectType.MAIN_CHARA) {
                    attack(pebble.attackValue)
                }
                if (pebble.isActive) {
                    pebble.handler.postDelayed(this, pebble.speed.toLong())
                }
            }
        }
        pebble.handler.postDelayed(runnableCode, pebble.speed.toLong())
    }

    fun endBossDefeated() {
        val xCoord = (field.size / 2)
        val yCoord = (field[xCoord].size / 2)
        val coordinatesLadder = Coordinates(xCoord - 1, yCoord)
        placeLadder(coordinatesLadder)

        val coordinatesTreasure = Coordinates(xCoord + 1, yCoord)
        placeTreasure(coordinatesTreasure, "treasure0")
    }

    fun fixateChara() {
        charaFixated = true
    }

    fun releaseChara() {
        charaFixated = false
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