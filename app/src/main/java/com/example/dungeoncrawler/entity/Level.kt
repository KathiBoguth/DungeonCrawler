package com.example.dungeoncrawler.entity

import android.content.Context
import android.util.Log
import androidx.compose.ui.unit.Dp
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import com.example.dungeoncrawler.entity.weapon.Arrow
import com.example.dungeoncrawler.entity.weapon.Pebble
import com.example.dungeoncrawler.service.FieldHelperService
import com.example.dungeoncrawler.service.FileReaderService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.URI
import java.util.concurrent.ThreadLocalRandom

class Level(
    private var chara: MainChara
) {

    companion object {
        const val SWORD_WOODEN = "sword_wooden"
        const val SWORD_IRON = "sword_iron"
        const val SWORD_DIAMOND = "sword_diamond"
        const val CUIRASS_RAG = "cuirass_rag"
        const val CUIRASS_IRON = "cuirass_iron"
        const val CUIRASS_DIAMOND = "cuirass_diamond"
        const val BOW_WOODEN = "bow_wooden"
        const val ARROW = "arrow"
    }

    private val fileReaderService = FileReaderService()
    lateinit var fieldHelperService: FieldHelperService

    var fieldLayout: List<List<GroundType>> = emptyList()

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

    var levelCount = 1

    fun initLevel(context: Context) {
        gameObjectIds.clear()
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
        fieldHelperService = FieldHelperService()
        fieldHelperService.initField(fieldLayout)
        val objectIds = fieldHelperService.initFieldObjects(endBoss, fieldLayout)
        if (objectIds.isEmpty()) {
            return false
        } else {
            gameObjectIds.addAll(objectIds)
        }
        movableEntitiesList.filterIsInstance<BasicEnemy>().forEach {
            it.destroy()
        }
        val newMovableEntities =
            fieldHelperService.initMovableObjects(levelCount, enemyPositionFlow)

        movableEntitiesList.clear()
        movableEntitiesList.addAll(newMovableEntities)
        movableEntitiesList.add(chara)
        fieldHelperService.placeChara(chara)
        chara.position = Settings.startCoordinates
        return true
    }

    private fun getRandomFieldLayout(): List<List<URI>> {
        val size = ThreadLocalRandom.current().nextInt(4) + 1
        val fieldLayout = mutableListOf<List<URI>>()
        for (i in 0..size) {
            val list = mutableListOf<URI>()
            for (j in 0..size) {
                val file = if (i == 0 && j == 0) {
                    Settings.startRoomUri
                } else {
                    Settings.roomFiles[ThreadLocalRandom.current().nextInt(Settings.roomFiles.size)]
                }
                list.add(file)
            }
            fieldLayout.add(list)
        }
        return fieldLayout
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
        fieldHelperService.field[coordinates.x][coordinates.y].add(arrow)
        arrowFlow = arrow.positionFlow

        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                val enemy = arrow.move(fieldHelperService.field, movableEntitiesList)
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
        fieldHelperService.field[coordinates.x][coordinates.y].add(pebble)
        pebbleFlow = pebble.positionFlow

        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                val chara = pebble.move(fieldHelperService.field, movableEntitiesList)
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
        val xCoord = (fieldLayout.size / 2)
        val yCoord = (fieldLayout[xCoord].size / 2)
        val coordinatesLadder = Coordinates(xCoord - 1, yCoord)
        val ladderId = fieldHelperService.placeLadder(coordinatesLadder, fieldLayout)
        if (ladderId != null) gameObjectIds.add(ladderId)

        val coordinatesTreasure = Coordinates(xCoord + 1, yCoord)
        val id = fieldHelperService.placeDiamondTreasure(coordinatesTreasure)
        gameObjectIds.add(id)
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