package com.example.dungeoncrawler.entity

enum class LevelObjectType {
    MAIN_CHARA,
    WALL,
    TREASURE,
    LADDER,
    ENEMY,
    COIN,
    WEAPON;

    fun isSteppableObject(): Boolean {
        return this == LADDER || this == COIN || this == WEAPON
    }
}