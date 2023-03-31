package com.example.dungeoncrawler

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.Coin
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import com.example.dungeoncrawler.entity.Level
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MainChara
import com.example.dungeoncrawler.entity.Potion
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.weapon.Bow
import com.example.dungeoncrawler.entity.weapon.Weapon
import kotlin.math.max
import kotlin.math.min

class GameViewModel : ViewModel() {

    var chara = MainChara()
    var killedBy = ""

    lateinit var level: Level

    val attackedEntityAnimation: MutableLiveData<String> by lazy { MutableLiveData() }
    val endGame: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val updateLevel: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    fun onEnemyPositionChange(levelObjectPositionChangeDTO: LevelObjectPositionChangeDTO) {
        if (!movePossible(levelObjectPositionChangeDTO.newPosition)) {
            return
        }

        val oldCoordinates = findCoordinate(levelObjectPositionChangeDTO.id)
        if(oldCoordinates.x == -1 && oldCoordinates.y == -1) {
            return
        }
        level.field[oldCoordinates.x][oldCoordinates.y].removeAll { it.id == levelObjectPositionChangeDTO.id }
        val enemy = level.enemies.find { it.id == levelObjectPositionChangeDTO.id }
        if (enemy != null) {
            level.field[levelObjectPositionChangeDTO.newPosition.x][levelObjectPositionChangeDTO.newPosition.y].add(enemy)
            enemy.position = levelObjectPositionChangeDTO.newPosition
        }

    }

    fun onEnemyAttack(damage: Int, enemyId: String) {
        val protection = chara.armor?.protection ?: 0
        chara.health -= max(0, (damage - (chara.baseDefense+protection)))
        if (chara.health <= 0) {
            killedBy = enemyId
            endGame.value = false
        }
    }

    fun interact() {
        var coordinates = findCoordinate(chara.id)

        coordinates = when (chara.direction) {
            Direction.UP -> Coordinates(coordinates.x, coordinates.y-1)
            Direction.DOWN -> Coordinates(coordinates.x, coordinates.y+1)
            Direction.LEFT -> Coordinates(coordinates.x-1, coordinates.y)
            Direction.RIGHT -> Coordinates(coordinates.x+1, coordinates.y)
        }

        val levelObjectList = level.field[coordinates.x][coordinates.y]
        if(levelObjectList.isEmpty()) {
            if (chara.weapon is Bow){
                if (!isArrowOnField()) {
                    throwArrow(coordinates, chara.direction)
                }
            }
            return
        }
        val enemy = levelObjectList.find { it.type == LevelObjectType.ENEMY }
        if (enemy != null) {
            attack(enemy as BasicEnemy)
            return
        }
        val levelObject = levelObjectList.first()
        when (levelObject.type) {
            LevelObjectType.TREASURE -> {
                levelObjectList.remove(levelObject)
                when (level.drop()) {
                    LevelObjectType.COIN -> placeCoin(coordinates)
                    LevelObjectType.POTION -> placePotion(coordinates)
                    LevelObjectType.WEAPON -> placeWeapon(coordinates)
                    LevelObjectType.ARMOR -> placeArmor(coordinates)
                    else -> {placeCoin(coordinates)}
                }
            }
            LevelObjectType.LADDER -> {
                nextLevel()
            }
            LevelObjectType.COIN -> {
                getRandomReward()
                levelObjectList.remove(levelObject)
            }
            LevelObjectType.POTION -> {
                heal((levelObject as Potion).hpCure)
                levelObjectList.remove(levelObject)
            }
            LevelObjectType.WEAPON -> {
                takeWeapon(coordinates)
            }
            LevelObjectType.ARMOR -> {
                takeArmor(coordinates)
            }
            else -> {}

        }
    }

    private fun isArrowOnField(): Boolean {
        return listOf("${Level.ARROW}_up", "${Level.ARROW}_up", "${Level.ARROW}_left", "${Level.ARROW}_right").any{
            findCoordinate(it) != Coordinates(-1, -1)
        }
    }

    private fun throwArrow(coordinates: Coordinates, direction: Direction) {
        level.throwArrow(coordinates, direction, ::attack)
    }

    private fun nextLevel() {
        val levelCount = level.levelCount
        level.levelCount = levelCount +1
        if (levelCount > Settings.levelsMax) {
            endGame.value = true
        } else {
            level.nextLevel.value = levelCount
        }
    }

    private fun takeWeapon(coordinates: Coordinates) {
        val oldWeapon = chara.weapon
        val weapon = level.field[coordinates.x][coordinates.y]
            .find { it.type == LevelObjectType.WEAPON } as Weapon

        chara.putOnWeapon(weapon)

        if (oldWeapon != null) {
            level.field[coordinates.x][coordinates.y].add(0, oldWeapon)
        } else {
            level.field[coordinates.x][coordinates.y].remove(weapon)
        }
    }

    private fun takeArmor(coordinates: Coordinates) {
        val oldArmor = chara.armor
        val armor = level.field[coordinates.x][coordinates.y]
            .find { it.type == LevelObjectType.ARMOR } as Armor

        chara.putOnArmor(armor)

        if (oldArmor != null) {
            level.field[coordinates.x][coordinates.y].add(0, oldArmor)
        } else {
            level.field[coordinates.x][coordinates.y].remove(armor)
        }
    }

    fun moveUp() {
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x, coordinates.y-1)

        moveIfPossible(newCoordinates, coordinates)
    }

    fun moveDown() {
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x, coordinates.y+1)
        moveIfPossible(newCoordinates, coordinates)
    }

    fun moveLeft() {
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x-1, coordinates.y)
        moveIfPossible(newCoordinates, coordinates)
    }

    fun moveRight() {
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x+1, coordinates.y)
        moveIfPossible(newCoordinates, coordinates)

    }

    private fun moveIfPossible(
        newCoordinates: Coordinates,
        coordinates: Coordinates
    ) {
        if (!movePossible(newCoordinates)) {
            return
        }

        level.field[coordinates.x][coordinates.y].remove(chara)
        val levelObjectList = level.field[newCoordinates.x][newCoordinates.y]
        //TODO: ConcurrentModificationException
        levelObjectList.forEach {
            when (it.type) {
                LevelObjectType.COIN -> {
                    getRandomReward()
                    levelObjectList.remove(it)
                }
                LevelObjectType.POTION -> {
                    heal((it as Potion).hpCure)
                }
                LevelObjectType.LADDER -> nextLevel()
                LevelObjectType.WEAPON -> takeWeapon(newCoordinates)
                LevelObjectType.ARMOR -> takeArmor(newCoordinates)

                else -> {}
            }
        }
        updateLevel.value = true

        levelObjectList.add(chara)
    }

    fun findCoordinate(id: String): Coordinates {
        for (row in 0 until level.field.size) {
            val index = level.field[row].indexOfFirst { it.indexOfFirst { levelObject -> levelObject.id == id  } != -1}
            if ( index != -1) {
                return Coordinates(row, index)
            }
        }
        return Coordinates(-1, -1)
    }

    fun turn(direction: Direction): Boolean {

        if (chara.direction == direction) {
            return false
        }
        chara.direction = direction
        return true
    }

    private fun attack(attackedEnemy: BasicEnemy) {
        val weaponBonus = chara.weapon?.attack ?: 0
        attackedEnemy.takeDamage(chara.baseAttack + weaponBonus)
        attackedEntityAnimation.value = attackedEnemy.id
        placeCoinIfEnemyDefeated(attackedEnemy)

    }

    private fun placeCoinIfEnemyDefeated(attackedEnemy: BasicEnemy) {
        if (attackedEnemy.health <= 0) {
            placeCoin(attackedEnemy.position)
            attackedEnemy.position = Coordinates(-1, -1)
        }
    }

    fun reset(newGame: Boolean = true, stats: SharedPreferences) {
        if (newGame) {
            chara = MainChara()
            chara.setBaseValues(stats)
            level = Level(chara)
        }
        endGame.value = null
    }

    private fun getRandomReward() {
        chara.gold += level.randomMoney(Settings.treasureMaxMoney)
    }

    private fun heal(hpCure: Int) {
        chara.health = min(hpCure + chara.health, chara.maxHealth)
    }

    private fun placeCoin(position: Coordinates){
        val coin = level.coinStack.removeFirst()
        level.field[position.x][position.y].add(Coin(coin))
        level.coinStack.addLast(coin)
    }

    private fun placePotion(position: Coordinates){
        val potion = level.potionStack.removeFirst()
        level.field[position.x][position.y].add(Potion(potion))
        level.coinStack.addLast(potion)
    }

    private fun placeWeapon(position: Coordinates){
        val weapon = level.randomWeapon()
        level.field[position.x][position.y].add(weapon)
    }

    private fun placeArmor(position: Coordinates){
        val armor = level.randomArmor()
        level.field[position.x][position.y].add(armor)
    }

    private fun movePossible(coordinates: Coordinates) : Boolean {
        if (coordinates.x >= level.field.size || coordinates.x < 0) {
            return false
        }
        if (coordinates.y >= level.field[coordinates.x].size || coordinates.y < 0) {
            return false
        }
        val levelObjectList = level.field[coordinates.x][coordinates.y]
        if ( levelObjectList.isNotEmpty() && levelObjectList.any { !it.type.isSteppableObject()} ) {
            return false
        }
        return true
    }


}