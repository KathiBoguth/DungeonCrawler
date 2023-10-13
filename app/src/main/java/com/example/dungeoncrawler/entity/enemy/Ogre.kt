package com.example.dungeoncrawler.entity.enemy

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.weapon.Pebble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class Ogre(ogreId: String, enemyPositionFlow: MutableStateFlow<LevelObjectPositionChangeDTO>) : BasicEnemy(ogreId, enemyPositionFlow) {

    override var speed = 800
    override var power = 80
    override var health = 500
    private var attackCharged = false

    val pebbleFlow: MutableStateFlow<Pebble?> = MutableStateFlow(null)

    override fun move(field: List<List<MutableList<LevelObject>>>) {
        if (health <= 0) {
            return
        }
        val charaNearby = attackCharaIfCloseBy(field)
        if (charaNearby) {
            enemyPositionFlow.update {
                LevelObjectPositionChangeDTO(position, direction, loadAttack = attackCharged, id)

            }
            return
        }
        attackCharged = false

        val charaPos = findChara(field)

        val horizontalDistance = charaPos.x - position.x
        val verticalDistance = charaPos.y - position.y

        val nextDirection = getNextDirection(horizontalDistance, verticalDistance, field)

        if (nextDirection != direction ) {
            direction = nextDirection
        } else {
            if (random.nextBoolean()) {
                throwPebble(position, direction)
            } else {
                position = moveOneStep()
            }

        }
        enemyPositionFlow.update { LevelObjectPositionChangeDTO(position, direction, false, id) }
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
            attackDamage.value = EnemyDamageDTO(
                random.nextInt(power - 10, power + 10),
                direction,
                EnemyEnum.OGRE,
                id
            )
            attackCharged = false
        }
    }

    private fun throwPebble(position: Coordinates, direction: Direction) {
        val pebblePosition = when (direction) {
            Direction.UP -> Coordinates(position.x, position.y - 1)
            Direction.DOWN -> Coordinates(position.x, position.y + 1)
            Direction.LEFT -> Coordinates(position.x - 1, position.y)
            Direction.RIGHT -> Coordinates(position.x + 1, position.y)
        }
        pebbleFlow.update {
            Pebble(direction, pebblePosition)
        }
    }
}