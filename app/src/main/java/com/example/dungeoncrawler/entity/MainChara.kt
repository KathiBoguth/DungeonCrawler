package com.example.dungeoncrawler.entity

import androidx.lifecycle.MutableLiveData
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.weapon.Weapon

class MainChara: MovableEntity(LevelObjectType.MAIN_CHARA, "character") {
    var health = 100
    var maxHealth = 100

    var gold = 0

    val baseAttack = 10
    var weapon: Weapon? = null
    var armor: Armor? = null

    val weaponObservable : MutableLiveData<Weapon> by lazy { MutableLiveData() }
    val armorObservable : MutableLiveData<Armor> by lazy { MutableLiveData() }

    fun putOnWeapon(newWeapon: Weapon) {
        weapon = newWeapon
        weaponObservable.value = newWeapon
    }

    fun putOnArmor(newArmor: Armor) {
        armor = newArmor
        armorObservable.value = newArmor
    }
}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}