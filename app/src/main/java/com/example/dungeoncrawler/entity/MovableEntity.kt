package com.example.dungeoncrawler.entity

abstract class MovableEntity(
    typeMovable: LevelObjectType,
    idMovable: String) : LevelObject(typeMovable, idMovable) {
    var direction = Direction.DOWN

}