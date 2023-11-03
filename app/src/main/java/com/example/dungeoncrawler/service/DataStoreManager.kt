package com.example.dungeoncrawler.service

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.CharaStats
import com.example.dungeoncrawler.data.DataStoreData
import com.example.dungeoncrawler.viewmodel.MenuViewModel
import com.example.dungeoncrawler.viewmodel.dataStore
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    companion object {
        const val GOLD_KEY = "goldValue"
        const val HEALTH_UPGRADE_COUNT_KEY = "healthUpgradeCount"
        const val ATTACK_UPGRADE_COUNT_KEY = "attackUpgradeCount"
        const val DEFENSE_UPGRADE_COUNT_KEY = "defenseUpgradeCount"
        const val HIGHSCORE_KEY = "highscore"
    }

    suspend fun saveToDataStore(dataStoreData: DataStoreData) {

        context.dataStore.edit { preferences ->
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

    suspend fun saveHighscoreToDataStore(gold: Int) {
        context.dataStore.edit { preferences ->
            val currentHighscore = preferences[intPreferencesKey(HIGHSCORE_KEY)] ?: 0
            if (gold > currentHighscore) {
                preferences[intPreferencesKey(HIGHSCORE_KEY)] = gold
            }
        }
    }

    fun getHighscoreData() = context.dataStore.data.map { preferences ->
        return@map preferences[intPreferencesKey(HIGHSCORE_KEY)] ?: 0
    }

    fun getDataFromDataStoreUpgradeScreen() = context.dataStore.data.map { preferences ->
        val gold = preferences[intPreferencesKey(GOLD_KEY)]
            ?: 0

        val healthUpgradeCount = preferences[intPreferencesKey(HEALTH_UPGRADE_COUNT_KEY)] ?: 0
        val attackUpgradeCount = preferences[intPreferencesKey(ATTACK_UPGRADE_COUNT_KEY)] ?: 0
        val defenseUpgradeCount = preferences[intPreferencesKey(DEFENSE_UPGRADE_COUNT_KEY)] ?: 0

        return@map DataStoreData(
            health = calcHealth(healthUpgradeCount),
            attack = calcAttack(attackUpgradeCount),
            defense = calcDefense(defenseUpgradeCount),
            gold = gold,
            healthUpgradeCount = healthUpgradeCount,
            attackUpgradeCount = attackUpgradeCount,
            defenseUpgradeCount = defenseUpgradeCount
        )

    }

    fun getDataFromDataStoreGameScreen() = context.dataStore.data.map { preferences ->
        val healthUpgradeCount = preferences[intPreferencesKey(HEALTH_UPGRADE_COUNT_KEY)] ?: 0
        val attackUpgradeCount = preferences[intPreferencesKey(ATTACK_UPGRADE_COUNT_KEY)] ?: 0
        val defenseUpgradeCount = preferences[intPreferencesKey(DEFENSE_UPGRADE_COUNT_KEY)] ?: 0

        return@map CharaStats(
            health = calcHealth(healthUpgradeCount),
            attack = calcAttack(attackUpgradeCount),
            defense = calcDefense(defenseUpgradeCount),
            gold = 0,
        )

    }

    private fun calcHealth(healthUpgradeCount: Int) =
        Settings.healthBaseValue + (healthUpgradeCount * MenuViewModel.HEALTH_UPGRADE_MULTIPLIER)

    private fun calcAttack(attackUpgradeCount: Int) = Settings.attackBaseValue + attackUpgradeCount
    private fun calcDefense(defenseUpgradeCount: Int) =
        Settings.defenseBaseValue + defenseUpgradeCount

    suspend fun clearDataStore() = context.dataStore.edit {
        it.clear()
    }
}