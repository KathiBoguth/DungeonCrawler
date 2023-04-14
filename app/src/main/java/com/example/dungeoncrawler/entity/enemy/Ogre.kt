package com.example.dungeoncrawler.entity.enemy

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import kotlin.math.abs

class Ogre(ogreId: String) : BasicEnemy(ogreId, "ogre") {

    override var speed = 800
    override var power = 80
    var attackCharged = false

    override fun move(field: Array<Array<MutableList<LevelObject>>>) {
        if(health <= 0) {
            return
        }
        val charaNearby = attackCharaIfCloseBy(field)
        if (charaNearby) {
            return
        }
        attackCharged = false

        val charaPos = findChara(field)

        val horizontalDistance = charaPos.x - position.x
        val verticalDistance = charaPos.y - position.y

        val nextDirection = getNextDirection(horizontalDistance, verticalDistance)

        if (nextDirection != direction ) {
            direction = nextDirection
        } else {
            val newPosition = moveOneStep()
            positionChange.value = LevelObjectPositionChangeDTO(newPosition, id)
        }
    }

    private fun findChara(field: Array<Array<MutableList<LevelObject>>>): Coordinates {
        var charaPos = Coordinates(-1, -1)
        for (row in field.indices) {
            val index = field[row].indexOfFirst { it.indexOfFirst { levelObject -> levelObject.id == "character"  } != -1}
            if ( index != -1) {
                charaPos = Coordinates(row, index)
            }
        }
        return charaPos
    }

    private fun getNextDirection(horizontalDistance: Int, verticalDistance: Int): Direction {
        return if (abs(horizontalDistance) > abs(verticalDistance)) {
            if (horizontalDistance > 0) {
                Direction.RIGHT
            } else {
                Direction.LEFT
            }
        } else {
            if (verticalDistance > 0) {
                Direction.DOWN
            } else {
                Direction.UP
            }
        }
    }

    override fun attack() {
        if (health > 0) {
            if (!attackCharged) {
                attackCharged = true
                return
            }
            attackDamage.value = EnemyDamageDTO(random.nextInt(power-10, power+10), direction, id)
            attackCharged = false
        }
    }
}