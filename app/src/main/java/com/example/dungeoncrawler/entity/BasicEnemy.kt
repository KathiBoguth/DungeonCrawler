package com.example.dungeoncrawler.entity

import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.dungeoncrawler.R
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BasicEnemy(idEnemy: String): MovableEntity(LevelObjectType.ENEMY, idEnemy) {

    var position = Coordinates(0,0)

    var health = 100

    var skin = "slime"

    var speed = 500

    val positionChange: MutableLiveData<Coordinates> by lazy { MutableLiveData() }

    private var handler = Handler(Looper.getMainLooper())
    private var random: Random = Random(System.currentTimeMillis())



    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            move()
            handler.postDelayed(this, speed.toLong())
        }
    }

    init {
        handler.postDelayed(runnableCode, speed.toLong())
    }

    fun move() {
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
            positionChange.value = newPosition
        }

    }
}