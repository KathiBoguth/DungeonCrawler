package com.example.dungeoncrawler.entity

class Bomb(id: String, val lit: Boolean = false) : LevelObject(LevelObjectType.BOMB, id)