package com.example.dungeoncrawler

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.Coin
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.enemy.EnemyPositionChangeDTO
import com.example.dungeoncrawler.entity.Level
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MainChara
import com.example.dungeoncrawler.entity.weapon.Weapon

class GameViewModel : ViewModel() {

    var chara = MainChara()
    var killedBy = ""

    lateinit var level: Level

    val attackedEntityAnimation: MutableLiveData<String> by lazy { MutableLiveData() }
    val endGame: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val updateLevel: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    fun onEnemyPositionChange(enemyPositionChangeDTO: EnemyPositionChangeDTO) {
        if (!movePossible(enemyPositionChangeDTO.newPosition)) {
            return
        }

        val oldCoordinates = findCoordinate(enemyPositionChangeDTO.id)
        if(oldCoordinates.x == -1 && oldCoordinates.y == -1) {
            return
        }
        level.field[oldCoordinates.x][oldCoordinates.y].removeAll { it.id == enemyPositionChangeDTO.id }
        val enemy = level.enemies.find { it.id == enemyPositionChangeDTO.id }
        if (enemy != null) {
            level.field[enemyPositionChangeDTO.newPosition.x][enemyPositionChangeDTO.newPosition.y].add(enemy)
            enemy.position = enemyPositionChangeDTO.newPosition
        }

    }

    fun onEnemyAttack(damage: Int, enemyId: String) {
        chara.health -= damage
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
                if(level.dropCoin()){
                    placeCoin(coordinates)
                } else {
                    placeWeapon(coordinates)
                }
            }
            LevelObjectType.LADDER -> {
                nextLevel()
            }
            LevelObjectType.COIN -> {
                getRandomReward()
                levelObjectList.remove(levelObject)
            }
            LevelObjectType.WEAPON -> {
                takeWeapon(coordinates)
            }
            else -> {}

        }
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
            level.field[coordinates.x][coordinates.y].add(oldWeapon)
        } else {
            level.field[coordinates.x][coordinates.y].remove(weapon)
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
        levelObjectList.forEach {
            when (it.type) {
                LevelObjectType.COIN -> {
                    getRandomReward()
                    levelObjectList.remove(it)
                }
                LevelObjectType.LADDER -> nextLevel()
                LevelObjectType.WEAPON -> takeWeapon(newCoordinates)

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
        if (attackedEnemy.health <= 0) {
            placeCoin(attackedEnemy.position)
            attackedEnemy.position = Coordinates(-1, -1)
        }
    }

    fun reset(newGame: Boolean = true) {
        if (newGame) {
            chara = MainChara()
            level = Level(chara)
        }
        endGame.value = null
    }

    private fun getRandomReward() {
        chara.gold += level.randomMoney(Settings.treasureMaxMoney)
    }

    private fun placeCoin(position: Coordinates){
        val coin = level.coinStack.removeFirst()
        level.field[position.x][position.y].add(Coin(coin))
        level.coinStack.addLast(coin)
    }

    private fun placeWeapon(position: Coordinates){
        val weapon = level.randomWeapon()
        level.field[position.x][position.y].add(weapon)
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