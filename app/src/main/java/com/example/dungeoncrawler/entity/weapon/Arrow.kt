package com.example.dungeoncrawler.entity.weapon

import android.os.Handler
import android.os.Looper
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MovableEntity
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class Arrow(id: String, arrowDirection: Direction, coordinates: Coordinates): MovableEntity(LevelObjectType.ARROW, id) {

    var handler = Handler(Looper.getMainLooper())
    var speed = 200
    var isActive = true
    private val _positionFlow = MutableStateFlow(
        LevelObjectPositionChangeDTO(
            Coordinates(-1, -1),
            Direction.DOWN,
            false,
            ""
        )
    )
    val positionFlow = _positionFlow.asStateFlow()

    init {
        direction = arrowDirection
        position = coordinates
        _positionFlow.update {
            LevelObjectPositionChangeDTO(
                position,
                direction,
                false,
                id
            )
        }
    }

    fun move(
        field: List<List<MutableList<LevelObject>>>,
        movableEntitiesList: List<MovableEntity>
    ): BasicEnemy? {

        field[position.x][position.y].remove(this)

        val currentPositionLevelObjectList = field[position.x][position.y]
        if (position.x < 0 || position.x >= field.size || position.y < 0 || position.y > field[position.x].size) {
            deactivateArrow()
            return null
        }
        val enemy =
            movableEntitiesList.firstOrNull { it.position.x == position.x && it.position.y == position.y }
        if (enemy != null) {
            deactivateArrow()
            return (enemy as BasicEnemy)

        }
        val isNotSteppable =
            currentPositionLevelObjectList.find { !it.type.isSteppableObject() } != null
        if (isNotSteppable) {
            deactivateArrow()
            return null
        }

        val nextCoordinates = when (direction) {
            Direction.UP -> Coordinates(position.x, position.y - 1)
            Direction.LEFT -> Coordinates(position.x - 1, position.y)
            Direction.DOWN -> Coordinates(position.x, position.y + 1)
            Direction.RIGHT -> Coordinates(position.x + 1, position.y)
        }
        position = nextCoordinates
        field[position.x][position.y].add(this)
        _positionFlow.update {
            it.copy(
                newPosition = position
            )
        }

        return null
    }

    private fun deactivateArrow() {
        position = Coordinates(-1, -1)
        _positionFlow.update {
            it.copy(
                newPosition = position
            )
        }
        isActive = false
    }
}