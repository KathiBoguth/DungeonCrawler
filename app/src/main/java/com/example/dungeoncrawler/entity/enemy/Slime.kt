package com.example.dungeoncrawler.entity.enemy

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject

class Slime(slimeId: String) : BasicEnemy(slimeId, "slime") {

    override var speed = 800
    override var power = 20

    override fun move(field: Array<Array<MutableList<LevelObject>>>) {
        if(health <= 0) {
            return
        }
        val charaNearby = attackCharaIfCloseBy(field)
        if (charaNearby) {
            return
        }

        val turn = random.nextFloat()

        if (turn > 0.8) {
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
            positionChange.value = LevelObjectPositionChangeDTO(newPosition, id)
        }
    }
}