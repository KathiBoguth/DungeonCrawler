package com.example.dungeoncrawler.entity.enemy

import android.os.Handler
import android.os.Looper
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

class Plant(plantId: String, enemyPositionFlow: MutableStateFlow<LevelObjectPositionChangeDTO>) :
    BasicEnemy(plantId, enemyPositionFlow) {

    override var speed = 800
    override var power = 40

    private var fixationReady = true
    private var fixationDuration = 3000L
    private var fixationAttackCooldown = 5000L
    private val range = 3

    val fixateCharaFlow = MutableStateFlow(false)

    override fun move(field: List<List<MutableList<LevelObject>>>) {
        if (health <= 0) {
            return
        }
        val charaNearby = attackCharaIfCloseBy(field)
        if (charaNearby) {
            enemyPositionFlow.update {
                LevelObjectPositionChangeDTO(position, direction, false, id)

            }
            return
        }

        val charaPos = findChara(field)

        val horizontalDistance = charaPos.x - position.x
        val verticalDistance = charaPos.y - position.y
        val charaInRange = abs(horizontalDistance) <= range && abs(verticalDistance) <= range
        if (fixationReady && charaInRange) {
            fixateCharaFlow.update {
                true
            }
            handleFixationAttackCooldown()
            handleCharaFixationcooldown()

            return
        }
        val nextDirection = if (charaInRange) {
            getNextDirection(horizontalDistance, verticalDistance, field)
        } else {
            val turn = random.nextFloat()
            if (turn > 0.8) {
                when (random.nextInt(4)) {
                    0 -> Direction.RIGHT
                    1 -> Direction.DOWN
                    2 -> Direction.LEFT
                    else -> Direction.UP
                }

            } else {
                direction
            }
        }
        if (nextDirection == direction) {
            if (canWalk(field)) {
                position = moveOneStep()
            }
        } else {
            direction = nextDirection
        }
        enemyPositionFlow.update {
            LevelObjectPositionChangeDTO(position, direction, false, id)
        }
    }

    private fun handleCharaFixationcooldown() {
        val runnableCodeFixationCooldown = Runnable {
            fixateCharaFlow.update {
                false
            }
        }
        Handler(Looper.getMainLooper()).postDelayed(runnableCodeFixationCooldown, fixationDuration)
    }

    private fun handleFixationAttackCooldown() {
        fixationReady = false
        val runnableCodeAttackCooldown = Runnable {
            fixationReady = true
        }
        Handler(Looper.getMainLooper()).postDelayed(
            runnableCodeAttackCooldown,
            fixationAttackCooldown
        )
    }
}