package com.example.dungeoncrawler.entity

abstract class MovableEntity(
    typeMovable: LevelObjectType,
    idMovable: String) : LevelObject(typeMovable, idMovable) {
    var direction = Direction.DOWN
    var position = Coordinates(-1,-1)

}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}