package com.example.dungeoncrawler.entity.enemy

import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class Slime(slimeId: String, enemyPositionFlow: MutableStateFlow<LevelObjectPositionChangeDTO>) : BasicEnemy(slimeId, enemyPositionFlow) {

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
            if(canWalk(field)){
                position = moveOneStep()
            }
        }
        enemyPositionFlow.update {
            LevelObjectPositionChangeDTO(position, direction, id)
        }
    }
}