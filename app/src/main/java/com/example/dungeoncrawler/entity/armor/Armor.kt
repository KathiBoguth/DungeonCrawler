package com.example.dungeoncrawler.entity.armor

import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType

abstract class Armor(val protection: Int, armorId: String) : LevelObject(LevelObjectType.ARMOR, armorId)