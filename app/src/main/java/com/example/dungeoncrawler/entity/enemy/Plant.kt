package com.example.dungeoncrawler.entity.enemy

import android.os.Handler
import android.os.Looper
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

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
        if (fixationReady && horizontalDistance <= range && verticalDistance <= range) {
            fixateCharaFlow.update {
                true
            }
            fixationReady = false
            val runnableCode = Runnable { fixationReady = true }
            Handler(Looper.getMainLooper()).postDelayed(runnableCode, fixationCooldown)
            return
        }

        val turn = random.nextFloat()

        if (turn > 0.8) {
            direction = when (random.nextInt(4)) {
                0 -> Direction.RIGHT
                1 -> Direction.DOWN
                2 -> Direction.LEFT
                else -> Direction.UP
            }

        } else {
            if (canWalk(field)) {
                position = moveOneStep()
            }
        }
        enemyPositionFlow.update {
            LevelObjectPositionChangeDTO(position, direction, false, id)
        }
    }
}