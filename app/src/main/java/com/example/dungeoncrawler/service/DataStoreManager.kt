package com.example.dungeoncrawler.service

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.DataStoreData
import com.example.dungeoncrawler.viewmodel.dataStore
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    companion object {
        const val HEALTH_KEY = "healthValue"
        const val ATTACK_KEY = "attackValue"
        const val DEFENSE_KEY = "defenseValue"
        const val GOLD_KEY = "goldValue"
        const val HEALTH_UPGRADE_COUNT_KEY = "healthUpgradeCount"
        const val ATTACK_UPGRADE_COUNT_KEY = "attackUpgradeCount"
        const val DEFENSE_UPGRADE_COUNT_KEY = "defenseUpgradeCount"
    }

    suspend fun saveToDataStore(dataStoreData: DataStoreData) {

        context.dataStore.edit { preferences ->

            preferences[intPreferencesKey(HEALTH_KEY)] = dataStoreData.health
            preferences[intPreferencesKey(ATTACK_KEY)] = dataStoreData.attack
            preferences[intPreferencesKey(DEFENSE_KEY)] = dataStoreData.defense
            preferences[intPreferencesKey(GOLD_KEY)] = dataStoreData.gold
            preferences[intPreferencesKey(HEALTH_UPGRADE_COUNT_KEY)] = dataStoreData.healthUpgradeCount
            preferences[intPreferencesKey(ATTACK_UPGRADE_COUNT_KEY)] = dataStoreData.attackUpgradeCount
            preferences[intPreferencesKey(DEFENSE_UPGRADE_COUNT_KEY)] = dataStoreData.defenseUpgradeCount
        }
    }

    suspend fun saveGatheredGoldToDataStore(gold: Int) {
        context.dataStore.edit { preferences ->
            val oldGold = preferences[intPreferencesKey(GOLD_KEY)]
            val newGold = oldGold?.plus(gold) ?: gold
            preferences[intPreferencesKey(GOLD_KEY)] = newGold
        }
    }


    fun getDataFromDataStore() = context.dataStore.data.map { preferences ->
        val health = preferences[intPreferencesKey(HEALTH_KEY)]
            ?: Settings.healthBaseValue
        val attack = preferences[intPreferencesKey(ATTACK_KEY)]
            ?: Settings.attackBaseValue
        val defense = preferences[intPreferencesKey(DEFENSE_KEY)]
            ?: Settings.defenseBaseValue
        val gold = preferences[intPreferencesKey(GOLD_KEY)]
            ?: 0

        val healthUpgradeCount = preferences[intPreferencesKey(HEALTH_UPGRADE_COUNT_KEY)] ?: 0
        val attackUpgradeCount = preferences[intPreferencesKey(ATTACK_UPGRADE_COUNT_KEY)] ?: 0
        val defenseUpgradeCount = preferences[intPreferencesKey(DEFENSE_UPGRADE_COUNT_KEY)] ?: 0

        return@map DataStoreData(
            health = health,
            attack = attack,
            defense = defense,
            gold = gold,
            healthUpgradeCount = healthUpgradeCount,
            attackUpgradeCount = attackUpgradeCount,
            defenseUpgradeCount = defenseUpgradeCount
        )

    }

    suspend fun clearDataStore() = context.dataStore.edit {
        it.clear()
    }
}