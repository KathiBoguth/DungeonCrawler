package com.example.dungeoncrawler.entity.enemy

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

class Ogre(ogreId: String, enemyPositionFlow: MutableStateFlow<LevelObjectPositionChangeDTO>) : BasicEnemy(ogreId, enemyPositionFlow) {

    override var speed = 800
    override var power = 80
    private var attackCharged = false

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
            position = moveOneStep()

        }
        enemyPositionFlow.update { LevelObjectPositionChangeDTO(position, direction, false, id) }
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
                enemyPositionFlow.update {
                    LevelObjectPositionChangeDTO(
                        newPosition = position,
                        newDirection = direction,
                        id = id,
                        loadAttack = true
                    )
                }
                return
            }
            attackDamage.value = EnemyDamageDTO(random.nextInt(power-10, power+10), direction, id)
            attackCharged = false
        }
    }
}