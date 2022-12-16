package com.example.dungeoncrawler.entity

import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.dungeoncrawler.R
import kotlin.math.max
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BasicEnemy(idEnemy: String, var field: Array<Array<LevelObject?>>): MovableEntity(LevelObjectType.ENEMY, idEnemy) {

    var position = Coordinates(0,0)

    var health = 100

    var skin = "slime"

    var speed = 500
    var power = 20

    val positionChange: MutableLiveData<EnemyPositionChangeDTO> by lazy { MutableLiveData() }
    val attackDamage: MutableLiveData<EnemyDamageDTO> by lazy { MutableLiveData() }



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
        if(health <= 0) {
            return
        }
        val directionChara = checkForChara()
        if( directionChara != null){
            if (direction != directionChara) {
                direction = directionChara
                return
            }
            attack()
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
            positionChange.value = EnemyPositionChangeDTO(newPosition, id)
        }

    }

    private fun attack() {
        if (!(health <= 0)) {
            attackDamage.value = EnemyDamageDTO(random.nextInt(power), id)
        }
    }

    private fun checkForChara(): Direction? {
        if(position.x != 0){
            val left = field[position.x-1][position.y]
            if (left != null && left.id == "character"){
                return Direction.LEFT
            }
        }
        if(position.x != field.size-1){
            val right = field[position.x+1][position.y]
            if (right != null && right.id == "character"){
                return Direction.RIGHT
            }
        }

        if(position.y != 0){
            val top = field[position.x][position.y-1]
            if (top != null && top.id == "character"){
                return Direction.UP
            }
        }

        if(position.y != field[position.x].size-1){
            val bottom = field[position.x][position.y+1]
            if (bottom != null && bottom.id == "character"){
                return Direction.DOWN
            }
        }
        return null
    }
}