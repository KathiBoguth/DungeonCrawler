package com.example.dungeoncrawler.entity

enum class LevelObjectType {
    MAIN_CHARA,
    WALL,
    TREASURE,
    LADDER,
    ENEMY,
    COIN,
    POTION,
    WEAPON,
    ARMOR;

    fun isSteppableObject(): Boolean {
        return this == LADDER || this == COIN || this == WEAPON || this == ARMOR || this == POTION
    }
}