package com.example.dungeoncrawler.entity

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.MenuViewModel
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

    fun setBaseValues(stats: SharedPreferences) {
        health = stats.getInt(MenuViewModel.HEALTH_KEY, Settings.healthBaseValue)
        maxHealth = health
        baseAttack = stats.getInt(MenuViewModel.ATTACK_KEY, Settings.attackBaseValue)
        baseDefense = stats.getInt(MenuViewModel.DEFENSE_KEY, Settings.defenseBaseValue)
    }
}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}