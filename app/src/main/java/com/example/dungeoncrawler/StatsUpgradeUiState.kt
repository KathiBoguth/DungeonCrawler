package com.example.dungeoncrawler

data class StatsUpgradeUiState (
    var initialData: CharaStats = CharaStats(0, 0, 0, 0),

    var healthUpgrade: Int = initialData.health,
    var attackUpgrade: Int = initialData.attack,
    var defenseUpgrade: Int = initialData.defense,
    var gold: Int = initialData.gold,
    var goldCost: Int = 0,

    var healthUpgradeButtonEnabled:Boolean = false,
    var attackUpgradeButtonEnabled:Boolean = false,
    var defenseUpgradeButtonEnabled:Boolean = false,

    var healthUpgradeAffordable:Boolean = false,
    var attackUpgradeAffordable:Boolean = false,
    var defenseUpgradeAffordable:Boolean = false
)