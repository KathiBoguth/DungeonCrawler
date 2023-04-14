package com.example.dungeoncrawler

import com.example.dungeoncrawler.entity.enemy.EnemyEnum

class Settings {

    companion object {
        const val healthBaseValue = 100
        const val attackBaseValue = 10
        const val defenseBaseValue = 0
        const val moveLength = 80f
        const val fieldSize = 12
        const val treasureMax = 4
        const val margin = 16f
        const val treasureMaxMoney = 100
        const val nudgeWidth = 20f
        const val animDuration = 100L
        const val levelsMax = 3

        val enemiesPerLevel = mapOf(
            1 to arrayOf(EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.SLIME),
            2 to arrayOf(EnemyEnum.SLIME, EnemyEnum.SLIME, EnemyEnum.WOLF),
            3 to arrayOf(EnemyEnum.WOLF, EnemyEnum.WOLF, EnemyEnum.WOLF),
            4 to arrayOf(EnemyEnum.OGRE)
        )
    }
}