package com.example.dungeoncrawler.entity.weapon

import android.os.Handler
import android.os.Looper
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType

class Arrow(id: String, val direction: Direction, private var coordinates: Coordinates): LevelObject(LevelObjectType.ARROW, id) {

    var handler = Handler(Looper.getMainLooper())
    var speed = 200

    fun move(field: Array<Array<MutableList<LevelObject>>>) {
        val nextCoordinates = when(direction) {
            Direction.UP -> Coordinates(coordinates.x, coordinates.y-1)
            Direction.LEFT -> Coordinates(coordinates.x-1, coordinates.y)
            Direction.DOWN -> Coordinates(coordinates.x, coordinates.y+1)
            Direction.RIGHT -> Coordinates(coordinates.x+1, coordinates.y)
        }
        val currentPositionLevelObjectList = field[coordinates.x][coordinates.y]
        if (coordinates.x < 0 || coordinates.x >= field.size || coordinates.y < 0 || coordinates.y > field[coordinates.x].size){
            currentPositionLevelObjectList.remove(this)
            return
        }
        val isNotSteppable = currentPositionLevelObjectList.find { !it.type.isSteppableObject()} != null
        if (isNotSteppable) {
            currentPositionLevelObjectList.remove(this)
            return
        }
        val isEnemy = currentPositionLevelObjectList.find { it.type == LevelObjectType.ENEMY} != null
        if (isEnemy) {
            // TODO attack enemy
            return
        }
        field[nextCoordinates.x][nextCoordinates.y].add(this)
        field[coordinates.x][coordinates.y].remove(this)
        coordinates = nextCoordinates
    }
}