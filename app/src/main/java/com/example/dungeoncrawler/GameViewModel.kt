package com.example.dungeoncrawler

import androidx.lifecycle.ViewModel
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.Level
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MainChara

class GameViewModel : ViewModel() {

    var chara = MainChara()
    lateinit var killedBy: String

    var level = Level(chara)

    fun onEnemyPositionChange(coordinates: Coordinates) {
        if (!movePossible(coordinates)) {
            return
        }

        val oldCoordinates = findCoordinate(level.enemy.id)
        if(oldCoordinates.x == -1 && oldCoordinates.y == -1) {
            return
        }
        level.field[oldCoordinates.x][oldCoordinates.y] = null
        level.field[coordinates.x][coordinates.y] = level.enemy
        level.enemy.position = coordinates
    }

    fun onEnemyAttack(damage: Int, enemyId: String) {
        chara.health -= damage
        if (chara.health <= 0) {
            killedBy = enemyId
        }
    }

    fun interact() : Boolean {
        var coordinates = findCoordinate(chara.id)

        coordinates = when (chara.direction) {
            Direction.UP -> Coordinates(coordinates.x, coordinates.y-1)
            Direction.DOWN -> Coordinates(coordinates.x, coordinates.y+1)
            Direction.LEFT -> Coordinates(coordinates.x-1, coordinates.y)
            Direction.RIGHT -> Coordinates(coordinates.x+1, coordinates.y)
        }

        return when (level.field[coordinates.x][coordinates.y]?.type) {
            LevelObjectType.TREASURE -> {
                getRandomReward()
                level.field[coordinates.x][coordinates.y] = null
                false
            }
            LevelObjectType.LADDER -> true
            LevelObjectType.ENEMY -> {
                attack()
                false
            }
            else -> false

        }
    }

    fun moveUp(id: String): Coordinates {
        val coordinates = findCoordinate(id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return Coordinates(0,0)
        }
        val newCoordinates = Coordinates(coordinates.x, coordinates.y-1)

        if (!movePossible(newCoordinates)) {
            return Coordinates(0,0)
        }

        level.field[coordinates.x][coordinates.y] = null
        level.field[newCoordinates.x][newCoordinates.y] = chara
        return Coordinates(0,-1)
    }

    fun moveDown(id: String): Coordinates {
        val coordinates = findCoordinate(id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return Coordinates(0,0)
        }
        val newCoordinates = Coordinates(coordinates.x, coordinates.y+1)
        if (!movePossible(newCoordinates)) {
            return Coordinates(0,0)
        }

        level.field[coordinates.x][coordinates.y] = null
        level.field[newCoordinates.x][newCoordinates.y] = chara
        return Coordinates(0,1)
    }

    fun moveLeft(id: String): Coordinates {
        val coordinates = findCoordinate(id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return Coordinates(0,0)
        }
        val newCoordinates = Coordinates(coordinates.x-1, coordinates.y)
        if (!movePossible(newCoordinates)) {
            return Coordinates(0,0)
        }

        level.field[coordinates.x][coordinates.y] = null
        level.field[newCoordinates.x][newCoordinates.y] = chara
        return Coordinates(-1,0)
    }

    fun moveRight(id: String): Coordinates {
        val coordinates = findCoordinate(id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return Coordinates(0,0)
        }
        val newCoordinates = Coordinates(coordinates.x+1, coordinates.y)
        if (!movePossible(newCoordinates)) {
            return Coordinates(0, 0)
        }

        level.field[coordinates.x][coordinates.y] = null
        level.field[newCoordinates.x][newCoordinates.y] = chara
        return Coordinates(1,0)

    }

    fun findCoordinate(id: String): Coordinates {
        for (row in 0 until level.field.size) {
            val index = level.field[row].indexOfFirst { it?.id == id }
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

    private fun attack() {
        level.enemy.health -= 20
    }

    fun reset() {
        chara = MainChara()
        level = Level(chara)
    }

    private fun getRandomReward() {
        chara.gold += level.randomMoney(Settings.treasureMaxMoney)
    }

    private fun movePossible(coordinates: Coordinates) : Boolean {
        if (coordinates.x >= level.field.size || coordinates.x < 0) {
            return false
        }
        if (coordinates.y >= level.field[coordinates.x].size || coordinates.y < 0) {
            return false
        }
        if ( level.field[coordinates.x][coordinates.y] != null) {
            return false
        }
        return true
    }


}