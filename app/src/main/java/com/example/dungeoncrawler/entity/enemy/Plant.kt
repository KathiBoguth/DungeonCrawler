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
    private var fixationCooldown = 5000L
    private val range = 3

    val fixateCharaFlow = MutableStateFlow(false)

    override fun move(field: List<List<MutableList<LevelObject>>>) {
        if (health <= 0) {
            return
        }
        val charaNearby = attackCharaIfCloseBy(field)
        if (charaNearby) {
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
            fixationReady = false
            val runnableCode = Runnable { fixationReady = true }
            Handler(Looper.getMainLooper()).postDelayed(runnableCode, fixationCooldown)
            return
        }
        val nextDirection = if (charaInRange) {
            getNextDirection(horizontalDistance, verticalDistance)
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
}