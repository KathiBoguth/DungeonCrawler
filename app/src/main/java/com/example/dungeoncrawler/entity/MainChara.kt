package com.example.dungeoncrawler.entity

import androidx.lifecycle.MutableLiveData
import com.example.dungeoncrawler.entity.weapon.Weapon
import java.util.Observable

class MainChara: MovableEntity(LevelObjectType.MAIN_CHARA, "character") {
    var health = 100

    var gold = 0

    val baseAttack = 10
    var weapon: Weapon? = null

    val weaponObservable : MutableLiveData<Weapon> by lazy { MutableLiveData() }

    fun putOnWeapon(newWeapon: Weapon) {
        weapon = newWeapon
        weaponObservable.value = newWeapon
    }
}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}