package com.example.dungeoncrawler.entity.enemy

import android.os.Handler
import android.os.Looper
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MovableEntity
import com.example.dungeoncrawler.entity.Wall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

abstract class BasicEnemy(idEnemy: String, val skin: String): MovableEntity(
    LevelObjectType.ENEMY, idEnemy) {



    var health = 100

    abstract var speed: Int
    abstract var power: Int

    val positionChange: MutableStateFlow<LevelObjectPositionChangeDTO> = MutableStateFlow(
        LevelObjectPositionChangeDTO(Coordinates(-1,-1), "")
    )
    val attackDamage: MutableStateFlow<EnemyDamageDTO> = MutableStateFlow(EnemyDamageDTO(0, Direction.UP, ""))

    var handler = Handler(Looper.getMainLooper())
    var random: Random = Random(System.currentTimeMillis())

    abstract fun move(field: Array<Array<MutableList<LevelObject>>>)

    fun takeDamage(damage: Int) {
        health -= damage
    }

    open fun attack() {
        if (health > 0) {
            attackDamage.value = EnemyDamageDTO(random.nextInt(power-10, power+10), direction, id)
        }
    }

    fun attackCharaIfCloseBy(field: Array<Array<MutableList<LevelObject>>>): Boolean {
        val directionChara = checkForChara(field)
        if( directionChara != null) {
            if (direction != directionChara) {
                direction = directionChara
                return true
            }
            attack()
            return true
        }
        return false
    }

    private fun checkForChara(field: Array<Array<MutableList<LevelObject>>>): Direction? {
        if(position.x != 0){
            val left = field[position.x-1][position.y]
            if (left.any { it.type == LevelObjectType.MAIN_CHARA }){
                return Direction.LEFT
            }
        }
        if(position.x != field.size-1){
            val right = field[position.x+1][position.y]
            if (right.any { it.type == LevelObjectType.MAIN_CHARA }){
                return Direction.RIGHT
            }
        }

        if(position.y != 0){
            val top = field[position.x][position.y-1]
            if (top.any { it.type == LevelObjectType.MAIN_CHARA }){
                return Direction.UP
            }
        }

        if(position.y != field[position.x].size-1){
            val bottom = field[position.x][position.y+1]
            if (bottom.any { it.type == LevelObjectType.MAIN_CHARA }){
                return Direction.DOWN
            }
        }
        return null
    }

    fun canWalk(field: Array<Array<MutableList<LevelObject>>>): Boolean {
        // TODO: needs movableEntities list for correct calculation
        val objectInFront = getObjectInFrontOfMe(field)
        return !objectInFront.any { !it.type.isSteppableObject() }
    }

    private fun getObjectInFrontOfMe(field: Array<Array<MutableList<LevelObject>>>): List<LevelObject> {
        val posBeforeMe = when(direction) {
            Direction.UP -> Coordinates(position.x, position.y-1)
            Direction.DOWN -> Coordinates(position.x, position.y+1)
            Direction.LEFT -> Coordinates(position.x-1, position.y)
            Direction.RIGHT -> Coordinates(position.x+1, position.y)
        }
        val isWall = when (direction) {
            Direction.UP -> posBeforeMe.y < 0
            Direction.DOWN -> posBeforeMe.y >= field[posBeforeMe.x].size
            Direction.LEFT -> posBeforeMe.x < 0
            Direction.RIGHT -> posBeforeMe.x >= field.size
        }
        return if (isWall) {
            listOf(Wall())
        } else {
            field[posBeforeMe.x][posBeforeMe.y]
        }
    }

    fun moveOneStep(): Coordinates {
        return when (direction) {
            Direction.UP -> {
                val posY = position.y -1
                Coordinates(position.x, posY)
            }
            Direction.DOWN -> {
                val posY = position.y+1
                Coordinates(position.x, posY)
            }
            Direction.LEFT -> {
                val posX = position.x-1
                Coordinates(posX, position.y)
            }
            Direction.RIGHT -> {
                val posX = position.x +1
                Coordinates(posX, position.y)
            }

        }
    }
}