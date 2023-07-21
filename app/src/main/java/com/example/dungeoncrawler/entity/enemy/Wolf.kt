package com.example.dungeoncrawler.entity.enemy

import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class Wolf(wolfId: String, enemyPositionFlow: MutableStateFlow<LevelObjectPositionChangeDTO>) : BasicEnemy(wolfId, enemyPositionFlow) {

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

        val turn = if (canWalk(field)) false else random.nextFloat() > 0.5

        if (turn) {
            direction = when(random.nextInt(4)) {
                0 -> Direction.RIGHT
                1 -> Direction.DOWN
                2 -> Direction.LEFT
                else -> Direction.UP
            }
        } else {
            position = moveOneStep()

        }
        enemyPositionFlow.update { LevelObjectPositionChangeDTO(position, direction, id) }
    }
}