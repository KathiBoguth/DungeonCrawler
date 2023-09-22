package com.example.dungeoncrawler.entity

enum class LevelObjectType {
    MAIN_CHARA,
    WALL,
    WATER, // steppable, but no objects allowed
    TREASURE,
    LADDER,
    ENEMY,
    COIN,
    POTION,
    WEAPON,
    ARROW,
    ARMOR;

    fun isSteppableObject(): Boolean {
        return this == LADDER || this == COIN || this == WEAPON || this == ARMOR || this == POTION || this == ARROW || this == WATER
    }
}