package com.example.dungeoncrawler.entity

import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.CharaStats
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.weapon.Weapon

class MainChara: MovableEntity(LevelObjectType.MAIN_CHARA, "character") {
    var health = Settings.healthBaseValue
    var maxHealth = Settings.healthBaseValue

    var gold = 0

    var baseAttack = Settings.attackBaseValue
    var baseDefense = Settings.defenseBaseValue
    var weapon: Weapon? = null
    var armor: Armor? = null


    fun putOnWeapon(newWeapon: Weapon) {
        weapon = newWeapon
    }

    fun putOnArmor(newArmor: Armor) {
        armor = newArmor
    }

    fun setBaseValues(charaStats: CharaStats) {
        health = charaStats.health
        maxHealth = health
        baseAttack = charaStats.attack
        baseDefense = charaStats.defense
    }
}

