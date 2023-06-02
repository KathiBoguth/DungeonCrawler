package com.example.dungeoncrawler

data class StatsUpgradeUiState (
    val initialData: CharaStats = CharaStats(0, 0, 0, 0),

    val healthUpgrade: Int = initialData.health,
    val attackUpgrade: Int = initialData.attack,
    val defenseUpgrade: Int = initialData.defense,
    val gold: Int = initialData.gold,
    val goldCost: Int = 0,

    val healthUpgradePlusButtonEnabled:Boolean = false,
    val attackUpgradePlusButtonEnabled:Boolean = false,
    val defenseUpgradePlusButtonEnabled:Boolean = false,

    val healthUpgradeMinusButtonEnabled:Boolean = false,
    val attackUpgradeMinusButtonEnabled:Boolean = false,
    val defenseUpgradeMinusButtonEnabled:Boolean = false,

    val isAnyUpgradeSelected:Boolean = false
)