package com.example.dungeoncrawler.entity.weapon

import android.os.Handler
import android.os.Looper
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.enemy.BasicEnemy

class Arrow(id: String, val direction: Direction, private var coordinates: Coordinates): LevelObject(LevelObjectType.ARROW, id) {

    var handler = Handler(Looper.getMainLooper())
    var speed = 200
    var isActive = true

    fun move(field: Array<Array<MutableList<LevelObject>>>) : BasicEnemy? {
        val nextCoordinates = when(direction) {
            Direction.UP -> Coordinates(coordinates.x, coordinates.y-1)
            Direction.LEFT -> Coordinates(coordinates.x-1, coordinates.y)
            Direction.DOWN -> Coordinates(coordinates.x, coordinates.y+1)
            Direction.RIGHT -> Coordinates(coordinates.x+1, coordinates.y)
        }
        val currentPositionLevelObjectList = field[coordinates.x][coordinates.y]
        if (coordinates.x < 0 || coordinates.x >= field.size || coordinates.y < 0 || coordinates.y > field[coordinates.x].size){
            currentPositionLevelObjectList.removeIf { this.id == it.id }
            isActive = false
            return null
        }
        val enemy = currentPositionLevelObjectList.find { it.type == LevelObjectType.ENEMY}
        if (enemy != null) {
            currentPositionLevelObjectList.removeIf { this.id == it.id }
            isActive = false
            return (enemy as BasicEnemy)

        }
        val isNotSteppable = currentPositionLevelObjectList.find { !it.type.isSteppableObject()} != null
        if (isNotSteppable) {
            currentPositionLevelObjectList.removeIf { this.id == it.id }
            isActive = false
            return null
        }

        field[nextCoordinates.x][nextCoordinates.y].add(this)
        field[coordinates.x][coordinates.y].removeIf { this.id == it.id }
        coordinates = nextCoordinates
        return null
    }
}