package com.example.dungeoncrawler.entity

class MainChara: MovableEntity(LevelObjectType.MAIN_CHARA, "character") {
    var health = 100

    var gold = 0
}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}