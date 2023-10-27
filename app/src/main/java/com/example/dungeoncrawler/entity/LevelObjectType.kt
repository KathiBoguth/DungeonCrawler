package com.example.dungeoncrawler.entity

enum class LevelObjectType {
    MAIN_CHARA,
    WALL,
    TREASURE,
    TREASURE_DIAMOND,
    LADDER,
    ENEMY,
    COIN,
    DIAMOND,
    POTION,
    WEAPON,
    ARROW,
    ARMOR;

    fun isSteppableObject(): Boolean {
        return this == LADDER || this == COIN || this == WEAPON || this == ARMOR || this == POTION || this == ARROW
    }
}