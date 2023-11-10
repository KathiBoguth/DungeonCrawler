package com.example.dungeoncrawler.service

import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.LevelObjectState
import com.example.dungeoncrawler.entity.Coin
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Diamond
import com.example.dungeoncrawler.entity.DiamondTreasure
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.GroundType
import com.example.dungeoncrawler.entity.Ladder
import com.example.dungeoncrawler.entity.Level.Companion.BOW_WOODEN
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_IRON
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_RAG
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_IRON
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_WOODEN
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MainChara
import com.example.dungeoncrawler.entity.MovableEntity
import com.example.dungeoncrawler.entity.Potion
import com.example.dungeoncrawler.entity.Treasure
import com.example.dungeoncrawler.entity.Wall
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.armor.Cuirass
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import com.example.dungeoncrawler.entity.enemy.Ogre
import com.example.dungeoncrawler.entity.enemy.Plant
import com.example.dungeoncrawler.entity.enemy.Slime
import com.example.dungeoncrawler.entity.enemy.Wolf
import com.example.dungeoncrawler.entity.weapon.Bow
import com.example.dungeoncrawler.entity.weapon.Sword
import com.example.dungeoncrawler.entity.weapon.Weapon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min


class FieldHelperService {
    var field: List<List<MutableList<LevelObject>>> = emptyList()

    private var coinCounter: Int = 0
    private var potionCounter: Int = 0

    var gamePaused = false

    private val weapons = listOf(
        Sword(10, SWORD_WOODEN),
        Sword(25, SWORD_IRON),
        Bow(10, BOW_WOODEN),
        Sword(50, SWORD_DIAMOND),
    )
    private val armors = listOf(
        Cuirass(10, CUIRASS_RAG),
        Cuirass(25, CUIRASS_IRON),
        Cuirass(50, CUIRASS_DIAMOND)
    )

    fun initField(fieldLayout: List<List<GroundType>>) {
        field = fieldLayout.map {
            it.map { groundType ->
                when (groundType) {
                    GroundType.STONE -> mutableListOf(Wall())
                    else -> mutableListOf()
                }
            }
        }
    }

    fun initFieldObjects(endBoss: Boolean, fieldLayout: List<List<GroundType>>): List<String> {
        placeWalls()
        val gameObjectIds = mutableListOf<String>()
        if (!endBoss) {
            val treasureList = placeTreasures()
            gameObjectIds.addAll(treasureList)
            val ladderId = placeLadder(fieldLayout = fieldLayout)
            if (ladderId == null) {
                return emptyList()
            } else {
                gameObjectIds.add(ladderId)
            }
        }
        return gameObjectIds
    }

    fun initMovableObjects(
        levelCount: Int,
        enemyPositionFlow: MutableStateFlow<LevelObjectPositionChangeDTO>
    ): List<MovableEntity> {
        return placeEnemies(levelCount, enemyPositionFlow)
    }

    fun interactWithAndRemoveLevelObject(
        coordinates: Coordinates,
        levelCount: Int
    ): Flow<ResultOfInteraction> {
        val levelObjectList = field[coordinates.x][coordinates.y]
        val levelObject = levelObjectList.firstOrNull()
        return flow {
            if (levelObject != null) {
                val removed = interactWithLevelObjectByType(levelObject, coordinates, levelCount)
                if (removed) levelObjectList.removeIf { it.id == levelObject.id }
            }
            emit(ResultOfInteraction.InteractionFinished)

        }
    }

    private suspend fun FlowCollector<ResultOfInteraction>.interactWithLevelObjectByType(
        levelObject: LevelObject,
        coordinates: Coordinates,
        levelCount: Int
    ): Boolean =
        when (levelObject.type) {
            LevelObjectType.TREASURE -> {
                when (drop()) {
                    LevelObjectType.COIN -> emit(placeCoin(coordinates))
                    LevelObjectType.POTION -> emit(placePotion(coordinates))
                    LevelObjectType.WEAPON -> emit(placeRandomWeapon(coordinates, levelCount))
                    LevelObjectType.ARMOR -> emit(placeRandomArmor(coordinates, levelCount))
                    else -> {
                        placeCoin(coordinates)
                    }
                }
                emit(ResultOfInteraction.RemoveLevelObject(levelObject.id))
                true
            }

            LevelObjectType.TREASURE_DIAMOND -> {
                placeDiamond(position = coordinates)
                emit(ResultOfInteraction.RemoveLevelObject(levelObject.id))
                true
            }

            LevelObjectType.LADDER -> {
                emit(ResultOfInteraction.NextLevel)
                true
            }

            LevelObjectType.COIN -> {
                takeCoin(levelObject as Coin)
                true
            }

            LevelObjectType.DIAMOND -> {
                takeDiamond(levelObject as Diamond)
                true
            }

            LevelObjectType.POTION -> {
                takePotion(levelObject as Potion)
                true
            }

            LevelObjectType.WEAPON -> {
                takeWeapon(coordinates)
                true
            }

            LevelObjectType.ARMOR -> {
                takeArmor(coordinates)
                true
            }

            LevelObjectType.MAIN_CHARA -> {
                false
            }

            LevelObjectType.WALL -> {
                false
            }

            LevelObjectType.ENEMY -> {
                false
            }

            LevelObjectType.ARROW -> {
                false
            }
        }

    // ----------- PLACE STUFF -------------

    private fun placeWalls() {
        for (row in field.indices) {
            for (column in field[row].indices) {
                if (row == 0 || column == 0 || row == field.size - 1 || column == field[row].size - 1) {
                    field[row][column].add(Wall())
                }
            }
        }
    }

    private fun placeTreasures(): List<String> {
        val treasureList = mutableListOf<String>()
        val treasureCount = ThreadLocalRandom.current().nextInt(1, Settings.treasureMax)
        for (i in 0..treasureCount) {
            val coordinates = randomFreeCoordinates(excluding = Settings.startArea)
            val treasureId = "treasure$i"
            placeTreasure(coordinates, treasureId)
            treasureList.add(treasureId)
        }
        return treasureList
    }

    private fun placeTreasure(coordinates: Coordinates, treasureId: String) {
        field[coordinates.x][coordinates.y].add(Treasure(treasureId))
    }

    fun placeDiamondTreasure(coordinates: Coordinates): String {
        val treasureId = "treasure0"
        field[coordinates.x][coordinates.y].add(DiamondTreasure(treasureId))
        return treasureId
    }

    fun placeLadder(
        coordinates: Coordinates? = null,
        fieldLayout: List<List<GroundType>>
    ): String? {
        val ladder = Ladder()
        if (coordinates != null) {
            field[coordinates.x][coordinates.y].add(ladder)
            return ladder.id
        }
        val fieldIntArray = PathFindingService.fieldToIntArray(fieldLayout)
        var randomCoordinates = randomFreeCoordinates(excluding = Settings.startArea)
        var isPathToLadder =
            PathFindingService.isPath(fieldIntArray, Settings.startCoordinates, randomCoordinates)
        for (i in 0..10) {
            if (isPathToLadder) {
                field[randomCoordinates.x][randomCoordinates.y].add(ladder)
                return ladder.id
            } else {
                randomCoordinates = randomFreeCoordinates(excluding = Settings.startArea)
                isPathToLadder = PathFindingService.isPath(
                    fieldIntArray,
                    Settings.startCoordinates,
                    randomCoordinates
                )
            }
        }
        return null
    }

    private fun placeEnemies(
        levelCount: Int,
        enemyPositionFlow: MutableStateFlow<LevelObjectPositionChangeDTO>
    ): List<MovableEntity> {
        val enemyList = ArrayList<BasicEnemy>()

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
        }
        return enemyList
    }

    fun placeChara(chara: MainChara) {
        field[Settings.startCoordinates.x][Settings.startCoordinates.y].add(chara)
    }

    private fun setMoveRunnable(enemy: BasicEnemy) {
        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                if (!gamePaused) {
                    enemy.move(field)
                }
                enemy.handler.postDelayed(this, enemy.speed.toLong())
            }
        }
        enemy.handler.postDelayed(runnableCode, enemy.speed.toLong())
    }

    private fun drop(): LevelObjectType {
        val randomValue = ThreadLocalRandom.current().nextFloat()
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

    private fun placeCoin(position: Coordinates): ResultOfInteraction {
        val coinId = "coin$coinCounter"
        coinCounter++
        return placeLevelObject(Coin(coinId), position)
    }

    fun placeCoinManually(position: Coordinates): Coin {
        val coinId = "coin$coinCounter"
        coinCounter++
        val coin = Coin(coinId)
        field[position.x][position.y].add(coin)
        return coin
    }

    private fun placeDiamond(position: Coordinates) {
        val diamondId = "diamond0"
        val diamond = Diamond(diamondId)
        field[position.x][position.y].add(diamond)
        ResultOfInteraction.AddLevelObject(
            LevelObjectState(
                diamondId,
                LevelObjectType.DIAMOND,
                position,
                Direction.DOWN
            )
        )
    }

    private fun placePotion(position: Coordinates): ResultOfInteraction {
        val potionId = "potion$potionCounter"
        potionCounter++
        field[position.x][position.y].add(Potion(potionId))
        return ResultOfInteraction.AddLevelObject(
            LevelObjectState(
                potionId,
                LevelObjectType.POTION,
                position,
                Direction.DOWN
            )
        )
    }

    private fun placeRandomWeapon(position: Coordinates, levelCount: Int): ResultOfInteraction {
        val weapon = randomWeapon(levelCount)
        return placeLevelObject(weapon, position)
    }

    private fun placeRandomArmor(position: Coordinates, levelCount: Int): ResultOfInteraction {
        val armor = randomArmor(levelCount)
        return placeLevelObject(armor, position)
    }

    fun placeLevelObject(levelObject: LevelObject, position: Coordinates): ResultOfInteraction {
        field[position.x][position.y].add(levelObject)
        return ResultOfInteraction.AddLevelObject(
            LevelObjectState(
                levelObject.id,
                levelObject.type,
                position,
                Direction.DOWN
            )
        )
    }

    // ----------- TAKE STUFF -------------

    private suspend fun FlowCollector<ResultOfInteraction>.takeWeapon(coordinates: Coordinates) {
        val weapon = field[coordinates.x][coordinates.y]
            .find { it.type == LevelObjectType.WEAPON } as Weapon

        emit(ResultOfInteraction.TakeWeapon(weapon))
        field[coordinates.x][coordinates.y].removeIf { it.id == weapon.id }
        emit(ResultOfInteraction.RemoveLevelObject(weapon.id))
    }

    private suspend fun FlowCollector<ResultOfInteraction>.takeArmor(coordinates: Coordinates) {
        val armor = field[coordinates.x][coordinates.y]
            .find { it.type == LevelObjectType.ARMOR } as Armor

        emit(ResultOfInteraction.TakeArmor(armor))

        field[coordinates.x][coordinates.y].removeIf { armor.id == it.id }
        emit(ResultOfInteraction.RemoveLevelObject(armor.id))
    }

    private suspend fun FlowCollector<ResultOfInteraction>.takePotion(potion: Potion) {
        emit(ResultOfInteraction.Heal(potion.hpCure))
        emit(ResultOfInteraction.RemoveLevelObject(potion.id))
    }

    private suspend fun FlowCollector<ResultOfInteraction>.takeCoin(coin: Coin) {
        emit(ResultOfInteraction.Reward(randomMoney(Settings.treasureMaxMoney)))
        emit(ResultOfInteraction.RemoveLevelObject(coin.id))
    }

    private suspend fun FlowCollector<ResultOfInteraction>.takeDiamond(diamond: Diamond) {
        emit(ResultOfInteraction.Reward(Settings.diamondWorth))
        emit(ResultOfInteraction.RemoveLevelObject(diamond.id))
    }

    fun isItemAtPosition(coordinates: Coordinates): Boolean {
        return field[coordinates.x][coordinates.y].isNotEmpty()
    }

    // ----------- RANDOM HELPER FUNCTIONS -------------

    private fun randomWeapon(levelCount: Int): Weapon {
        val randomValue = ThreadLocalRandom.current().nextFloat()
        val diamondProbability = min(0.4, max(levelCount - 7, 0) * 0.1)
        val bowProbability = min(0.4, max(levelCount - 4, 0) * 0.1)
        val ironProbability = min(0.4, max(levelCount - 3, 0) * 0.1)

        return if (randomValue < diamondProbability) {
            weapons.find { it.id == SWORD_DIAMOND } ?: weapons.first()
        } else if (randomValue < diamondProbability + bowProbability) {
            weapons.find { it.id == BOW_WOODEN } ?: weapons.first()
        } else if (randomValue < min(diamondProbability + bowProbability + ironProbability, 0.9)) {
            weapons.find { it.id == BOW_WOODEN } ?: weapons.first()
        } else {
            weapons.find { it.id == SWORD_WOODEN } ?: weapons.first()
        }
    }

    private fun randomArmor(levelCount: Int): Armor {
        val randomValue = ThreadLocalRandom.current().nextFloat()
        val diamondProbability = min(0.4, max(levelCount - 7, 0) * 0.1)
        val ironProbability = min(0.4, max(levelCount - 3, 0) * 0.1)

        return if (randomValue < diamondProbability) {
            armors.find { it.id == CUIRASS_DIAMOND } ?: armors.first()
        } else if (randomValue < diamondProbability + ironProbability) {
            armors.find { it.id == CUIRASS_IRON } ?: armors.first()
        } else {
            armors.find { it.id == CUIRASS_RAG } ?: armors.first()
        }
    }

    fun randomMoney(max: Int): Int {
        return ThreadLocalRandom.current().nextInt(max)
    }

    private fun randomFreeCoordinates(
        including: Coordinates = Coordinates(field.size, field[0].size),
        excluding: Coordinates = Coordinates(0, 0),
        movableEntitiesList: List<MovableEntity> = emptyList()
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

    private fun getRandomCoordinates(
        bounds: Coordinates = Coordinates(
            field.size,
            field[0].size
        )
    ): Coordinates {
        val xCord = ThreadLocalRandom.current().nextInt(bounds.x)
        val yCord = ThreadLocalRandom.current().nextInt(bounds.y)

        return Coordinates(xCord, yCord)
    }

    private fun randomDirection(): Direction {
        return when (ThreadLocalRandom.current().nextInt(4)) {
            0 -> Direction.UP
            1 -> Direction.DOWN
            2 -> Direction.LEFT
            3 -> Direction.RIGHT
            else -> Direction.DOWN
        }
    }
}

sealed class ResultOfInteraction {
    object NextLevel : ResultOfInteraction()
    object InteractionFinished : ResultOfInteraction()
    data class AddLevelObject(val levelObject: LevelObjectState) : ResultOfInteraction()
    data class RemoveLevelObject(val id: String) : ResultOfInteraction()
    data class TakeWeapon(val weapon: Weapon) : ResultOfInteraction()
    data class TakeArmor(val armor: Armor) : ResultOfInteraction()
    data class Heal(val heal: Int) : ResultOfInteraction()
    data class Reward(val amount: Int) : ResultOfInteraction()
}
