package com.example.dungeoncrawler.entity.enemy

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject

class Wolf(wolfId: String) : BasicEnemy(wolfId, "wolf") {

    override var speed = 400
    override var power = 30
    override fun move(field: Array<Array<MutableList<LevelObject>>>) {
        if(health <= 0) {
            return
        }
        val charaNearby = attackCharaIfCloseBy(field)
        if (charaNearby) {
            return
        }

        val turn = if (canWalk(field)) true else random.nextFloat() > 0.5

        if (turn) {
            direction = when(random.nextInt(4)) {
                0 -> Direction.RIGHT
                1 -> Direction.DOWN
                2 -> Direction.LEFT
                else -> Direction.UP
            }
        } else {
            val newPosition = when (direction) {
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
            positionChange.value = EnemyPositionChangeDTO(newPosition, id)
        }
    }
}