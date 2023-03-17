package com.example.dungeoncrawler.entity

import androidx.lifecycle.MutableLiveData
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.armor.Cuirass
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import com.example.dungeoncrawler.entity.enemy.Slime
import com.example.dungeoncrawler.entity.enemy.Wolf
import com.example.dungeoncrawler.entity.weapon.Arrow
import com.example.dungeoncrawler.entity.weapon.Bow
import com.example.dungeoncrawler.entity.weapon.Sword
import com.example.dungeoncrawler.entity.weapon.Weapon
import kotlin.random.Random

class Level(
    private var chara: MainChara,
) {

    companion object
    {
        private const val SWORD_WOODEN = "sword_wooden"
        private const val SWORD_DIAMOND = "sword_diamond"
        private const val CUIRASS_RAG = "cuirass_rag"
        private const val CUIRASS_IRON = "cuirass_iron"
        private const val CUIRASS_DIAMOND = "cuirass_diamond"
        private const val BOW_WOODEN = "bow_wooden"
        const val ARROW = "arrow"
    }
    var field: Array<Array<MutableList<LevelObject>>> = Array(Settings.fieldSize) {
        (Array(Settings.fieldSize) { mutableListOf() })
    }

    lateinit var enemies: MutableList<BasicEnemy>
    val coinStack = ArrayDeque<String>()
    val potionStack = ArrayDeque<String>()
    lateinit var weapons: MutableList<Weapon>
    lateinit var armors: MutableList<Armor>
    val gameObjectIds: MutableList<String> = mutableListOf()

    val nextLevel: MutableLiveData<Int> by lazy { MutableLiveData() }
    val arrowLiveData: MutableLiveData<LevelObjectPositionChangeDTO> by lazy { MutableLiveData() }

    private var random: Random = Random(System.currentTimeMillis())
    var levelCount = 1

    init {
        initLevel()
    }

    fun initLevel() {
        gameObjectIds.clear()
        gameObjectIds.addAll(listOf(SWORD_WOODEN, SWORD_DIAMOND, CUIRASS_RAG, CUIRASS_IRON,
            CUIRASS_DIAMOND, BOW_WOODEN, "${ARROW}_left", "${ARROW}_right", "${ARROW}_up", "${ARROW}_down"))
        field = Array(Settings.fieldSize) {
            (Array(Settings.fieldSize) { mutableListOf() })
        }
        weapons = mutableListOf(Sword(10, SWORD_WOODEN), Sword(50, SWORD_DIAMOND), Bow(10, BOW_WOODEN))
        armors = mutableListOf(Cuirass(10, CUIRASS_RAG), Cuirass(25, CUIRASS_IRON), Cuirass(50, CUIRASS_DIAMOND))
        placeWalls()
        placeTreasures()
        placeLadder()
        placeEnemies()
        fillCoinStack()
        fillPotionStack()
        var randomStartCoordinates = getRandomCoordinates()
        while (field[randomStartCoordinates.x][randomStartCoordinates.y].isNotEmpty()) {
            randomStartCoordinates = getRandomCoordinates()
        }
        field[randomStartCoordinates.x][randomStartCoordinates.y].add(chara)
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
            gameObjectIds.add(treasureId)
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
        Settings.enemiesPerLevel[levelCount]?.forEachIndexed { i, enemyType ->
            var coordinates = getRandomCoordinates()
            var levelObjectsList = field[coordinates.x][coordinates.y]
            while (levelObjectsList.isNotEmpty() || levelObjectsList.any { !it.type.isSteppableObject() }) {
                coordinates = getRandomCoordinates()
                levelObjectsList = field[coordinates.x][coordinates.y]
            }
            val enemy = when(enemyType) {
                // TODO: improve iterator (currently: slime1, slime2, wolf3, desired: slime1, slime2, wolf1)
                EnemyEnum.SLIME -> {
                    val count = enemyList.count { alreadyAdded ->  alreadyAdded.id.contains("slime") }
                    Slime("slime$count")
                }
                EnemyEnum.WOLF -> {
                    val count = enemyList.count { alreadyAdded ->  alreadyAdded.id.contains("wolf") }
                    Wolf("wolf$count")
                }

            }
            setMoveRunnable(enemy)

            field[coordinates.x][coordinates.y].add(enemy)
            enemy.position = coordinates
            enemy.direction = randomDirection()
            enemyList.add(enemy)
            gameObjectIds.add(enemy.id)
        }
        enemies = enemyList.toMutableList()

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
        coinIds.forEach{
            coinStack.addLast(it)
        }
        gameObjectIds.addAll(coinIds)
    }

    private fun fillPotionStack() {
        val potionIds = listOf("potion0", "potion1", "potion2")
        potionIds.forEach{
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
        return when (random.nextInt(4)){
            0 -> Direction.UP
            1 -> Direction.DOWN
            2 -> Direction.LEFT
            3 -> Direction.RIGHT
            else -> Direction.DOWN
        }
    }

    fun drop(): LevelObjectType {
        // TODO remove
        return LevelObjectType.WEAPON

        val randomValue = random.nextFloat()
        return if (randomValue < 0.35) {
            LevelObjectType.COIN
        } else if (randomValue < 0.6) {
            LevelObjectType.POTION
        } else if (randomValue < 0.85){
            LevelObjectType.ARMOR
        } else {
            LevelObjectType.WEAPON
        }

    }

    fun randomWeapon(): Weapon {
        val randomValue = random.nextFloat()

        // TODO: remove
        val bow = weapons.find { it.id == BOW_WOODEN } ?: weapons.first()
        weapons.remove(bow)
        return bow

        return if (randomValue < 0.4) {
            val bow = weapons.find { it.id == BOW_WOODEN } ?: weapons.first()
            weapons.remove(bow)
            bow
        } else if (randomValue < 0.8){
            val sword = weapons.find { it.id == SWORD_WOODEN } ?: weapons.first()
            weapons.remove(sword)
            sword
        } else {
            val sword = weapons.find { it.id == SWORD_DIAMOND } ?: weapons.first()
            weapons.remove(sword)
            sword
        }
    }

    fun randomArmor(): Armor {
        val randomValue = random.nextFloat()

        if (randomValue < 0.5) {
            val armor = armors.find { it.id == CUIRASS_RAG } ?: armors.first()
            armors.remove(armor)
            return armor
        } else if (randomValue < 0.8) {
            val armor = armors.find { it.id == CUIRASS_IRON } ?: armors.first()
            armors.remove(armor)
            return armor
        }
        val armor = armors.find { it.id == CUIRASS_DIAMOND } ?: armors.first()
        armors.remove(armor)
        return armor
    }

    fun throwArrow(coordinates: Coordinates, direction: Direction) {

        val id = when(direction){
            Direction.UP -> "arrow_up"
            Direction.LEFT -> "arrow_left"
            Direction.DOWN -> "arrow_down"
            Direction.RIGHT -> "arrow_right"
        }
        val arrow = Arrow(id, direction, coordinates)
        field[coordinates.x][coordinates.y].add(arrow)

        val runnableCode: Runnable = object : Runnable {
            override fun run() {
                arrow.move(field)
                arrow.handler.postDelayed(this, arrow.speed.toLong())
            }
        }
        arrow.handler.postDelayed(runnableCode, arrow.speed.toLong())
    }

}

class Coordinates(
    val x: Int,
    val y: Int
)