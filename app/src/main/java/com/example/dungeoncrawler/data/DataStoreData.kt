package com.example.dungeoncrawler.data

data class DataStoreData (
    val health: Int,
    val attack: Int,
    val defense: Int,
    val gold: Int,

    val healthUpgradeCount: Int,
    val attackUpgradeCount: Int,
    val defenseUpgradeCount: Int
)