package com.example.dungeoncrawler.entity.weapon

import com.example.dungeoncrawler.entity.CanStandOn
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType

abstract class Weapon(val attack: Int, weaponId: String) : LevelObject(LevelObjectType.WEAPON, weaponId), CanStandOn