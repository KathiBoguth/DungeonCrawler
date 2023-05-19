package com.example.dungeoncrawler.entity.weapon

import android.os.Handler
import android.os.Looper
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MovableEntity
import com.example.dungeoncrawler.entity.enemy.BasicEnemy

class Arrow(id: String, arrowDirection: Direction, coordinates: Coordinates): MovableEntity(LevelObjectType.ARROW, id) {

    var handler = Handler(Looper.getMainLooper())
    var speed = 200
    var isActive = true

    init {
        direction = arrowDirection
        position = coordinates
    }

    fun move(field: Array<Array<MutableList<LevelObject>>>, movableEntitiesList: List<MovableEntity>) : BasicEnemy? {

        val currentPositionLevelObjectList = field[position.x][position.y]
        if (position.x < 0 || position.x >= field.size || position.y < 0 || position.y > field[position.x].size){
            deactivateArrow()
            return null
        }
        val enemy = movableEntitiesList.firstOrNull { it.position.x == position.x && it.position.y == position.y }
        if (enemy != null) {
            deactivateArrow()
            return (enemy as BasicEnemy)

        }
        val isNotSteppable = currentPositionLevelObjectList.find { !it.type.isSteppableObject()} != null
        if (isNotSteppable) {
            deactivateArrow()
            return null
        }

        val nextCoordinates = when(direction) {
            Direction.UP -> Coordinates(position.x, position.y-1)
            Direction.LEFT -> Coordinates(position.x-1, position.y)
            Direction.DOWN -> Coordinates(position.x, position.y+1)
            Direction.RIGHT -> Coordinates(position.x+1, position.y)
        }
        position = nextCoordinates
        return null
    }

    private fun deactivateArrow() {
        position = Coordinates(-1, -1)
        isActive = false
    }
}