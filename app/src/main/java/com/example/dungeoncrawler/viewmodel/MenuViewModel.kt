package com.example.dungeoncrawler.viewmodel

import android.content.Context
import android.media.MediaPlayer
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeoncrawler.CharaStats
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.StatsUpgradeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(
    name = MenuViewModel.SAVED_STATS_KEY
)
class MenuViewModel : ViewModel() {

    companion object {
        const val HEALTH_UPGRADE_MULTIPLIER = 5
        const val COST_PER_UPGRADE = 50
        const val SAVED_STATS_KEY = "savedStats"

        const val HEALTH_KEY = "healthValue"
        const val ATTACK_KEY = "attackValue"
        const val DEFENSE_KEY = "defenseValue"
        const val GOLD_KEY = "goldValue"
        const val HEALTH_UPGRADE_COUNT_KEY = "healthUpgradeCount"
        const val ATTACK_UPGRADE_COUNT_KEY = "attackUpgradeCount"
        const val DEFENSE_UPGRADE_COUNT_KEY = "defenseUpgradeCount"
    }

    var initialData = CharaStats(0, 0, 0, 0)
    private var initialUpgradeCount = CharaStats(0,0,0, 0)

    var gold = initialData.gold

    private val _statsUpgradeUiState = MutableStateFlow(
        StatsUpgradeUiState()
    )
    val uiState = _statsUpgradeUiState.asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = StatsUpgradeUiState()
        )

    private lateinit var mediaPlayer: MediaPlayer

    fun onHealthPlusButtonClicked() {
        val cost = calcCost(_statsUpgradeUiState.value.healthUpgrade)
        if (cost < _statsUpgradeUiState.value.gold-_statsUpgradeUiState.value.goldCost) {
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost+cost
                val newHealthUpgrade = it.healthUpgrade+1
                it.copy(
                    goldCost = newGoldCost,
                    healthUpgrade = newHealthUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(newHealthUpgrade, newGoldCost),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(it.attackUpgrade, newGoldCost),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(it.defenseUpgrade, newGoldCost),
                    healthUpgradeMinusButtonEnabled = isUpgradeSelected(newHealthUpgrade, initialUpgradeCount.health),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(newHealthUpgrade, it.attackUpgrade, it.defenseUpgrade)
                )
            }
        }
    }

    fun onHealthMinusButtonClicked() {
        if (_statsUpgradeUiState.value.healthUpgrade > 0) {
            val cost = calcCost(max(0,_statsUpgradeUiState.value.healthUpgrade-1))
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost-cost
                val newHealthUpgrade = it.healthUpgrade-1
                it.copy(
                    goldCost = newGoldCost,
                    healthUpgrade = newHealthUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(newHealthUpgrade, newGoldCost),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(it.attackUpgrade, newGoldCost),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(it.defenseUpgrade, newGoldCost),
                    healthUpgradeMinusButtonEnabled = isUpgradeSelected(newHealthUpgrade, initialUpgradeCount.health),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(newHealthUpgrade, it.attackUpgrade, it.defenseUpgrade)
                )
            }
        }
    }

    fun onAttackPlusButtonClicked() {
        val cost = calcCost(_statsUpgradeUiState.value.attackUpgrade)
        if (cost <= _statsUpgradeUiState.value.gold-_statsUpgradeUiState.value.goldCost) {
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost+cost
                val newAttackUpgrade = it.attackUpgrade+1
                it.copy(
                    goldCost = newGoldCost,
                    attackUpgrade = it.attackUpgrade+1,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(it.healthUpgrade, newGoldCost),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(newAttackUpgrade, newGoldCost),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(it.defenseUpgrade, newGoldCost),
                    attackUpgradeMinusButtonEnabled = isUpgradeSelected(newAttackUpgrade, initialUpgradeCount.attack),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(it.healthUpgrade, newAttackUpgrade, it.defenseUpgrade)
                )
            }
        }
    }

    fun onAttackMinusButtonClicked() {
        if (_statsUpgradeUiState.value.attackUpgrade > 0) {
            val cost = calcCost(max(0, _statsUpgradeUiState.value.attackUpgrade-1))
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost-cost
                val newAttackUpgrade = it.attackUpgrade-1
                it.copy(
                    goldCost = newGoldCost,
                    attackUpgrade = newAttackUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(it.healthUpgrade, newGoldCost),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(newAttackUpgrade, newGoldCost),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(it.defenseUpgrade, newGoldCost),
                    attackUpgradeMinusButtonEnabled = isUpgradeSelected(newAttackUpgrade, initialUpgradeCount.attack),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(it.healthUpgrade, newAttackUpgrade, it.defenseUpgrade)
                )
            }
        }
    }

    fun onDefensePlusButtonClicked() {
        val cost = calcCost(_statsUpgradeUiState.value.defenseUpgrade)
        if (cost <= _statsUpgradeUiState.value.gold-_statsUpgradeUiState.value.goldCost) {
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost+cost
                val newDefenseUpgrade = it.defenseUpgrade+1
                it.copy(
                    goldCost = newGoldCost,
                    defenseUpgrade = newDefenseUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(it.healthUpgrade, newGoldCost),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(it.attackUpgrade, newGoldCost),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(newDefenseUpgrade, newGoldCost),
                    defenseUpgradeMinusButtonEnabled = isUpgradeSelected(newDefenseUpgrade, initialUpgradeCount.defense),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(it.healthUpgrade, it.attackUpgrade, newDefenseUpgrade)
                )
            }
        }
    }

    fun onDefenseMinusButtonClicked() {
        if (_statsUpgradeUiState.value.defenseUpgrade > 0) {
            val cost = calcCost(max(0, _statsUpgradeUiState.value.defenseUpgrade-1))
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost-cost
                val newDefenseUpgrade = it.defenseUpgrade-1
                it.copy(
                    goldCost = newGoldCost,
                    defenseUpgrade = newDefenseUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(it.healthUpgrade, newGoldCost),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(it.attackUpgrade, newGoldCost),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(newDefenseUpgrade, newGoldCost),
                    defenseUpgradeMinusButtonEnabled = isUpgradeSelected(newDefenseUpgrade, initialUpgradeCount.defense),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(it.healthUpgrade, it.attackUpgrade, newDefenseUpgrade)
                )
            }
        }
    }

    private fun isUpgradeAffordable(upgradeCount: Int, goldCost: Int): Boolean {
        val cost = calcCost(upgradeCount)
        return cost <= gold-goldCost
    }

    private fun isUpgradeSelected(upgradeCount: Int, initialUpgrade: Int): Boolean =
        upgradeCount > initialUpgrade

    fun reset() {
        _statsUpgradeUiState.update {
            it.copy(
                healthUpgrade = initialUpgradeCount.health,
                attackUpgrade = initialUpgradeCount.attack,
                defenseUpgrade = initialUpgradeCount.defense,
                healthUpgradePlusButtonEnabled = isUpgradeAffordable(initialUpgradeCount.health, 0),
                healthUpgradeMinusButtonEnabled = false,
                attackUpgradePlusButtonEnabled = isUpgradeAffordable(initialUpgradeCount.attack, 0),
                attackUpgradeMinusButtonEnabled = false,
                defenseUpgradePlusButtonEnabled = isUpgradeAffordable(initialUpgradeCount.defense, 0),
                defenseUpgradeMinusButtonEnabled = false,
                isAnyUpgradeSelected = false,
                goldCost = 0
            )
        }
    }

    fun loadStats(context: Context) {

        viewModelScope.launch {
            context.dataStore.data.collect{preferences ->
                val health = preferences[intPreferencesKey(HEALTH_KEY)] ?: Settings.healthBaseValue
                val healthUpgradeCount = preferences[intPreferencesKey(HEALTH_UPGRADE_COUNT_KEY)] ?: 0
                val attack = preferences[intPreferencesKey(ATTACK_KEY)] ?: Settings.attackBaseValue
                val attackUpgradeCount = preferences[intPreferencesKey(ATTACK_UPGRADE_COUNT_KEY)] ?: 0
                val defense = preferences[intPreferencesKey(DEFENSE_KEY)] ?: Settings.defenseBaseValue
                val defenseUpgradeCount = preferences[intPreferencesKey(DEFENSE_UPGRADE_COUNT_KEY)] ?: 0
                gold = preferences[intPreferencesKey(GOLD_KEY)] ?: 0

                initialData = CharaStats(health, attack, defense, gold)
                initialUpgradeCount = CharaStats(healthUpgradeCount, attackUpgradeCount, defenseUpgradeCount, 0)
                // TODO: get this and fix this
                _statsUpgradeUiState.update {
                    StatsUpgradeUiState(
                        initialData = initialData,
                        healthUpgrade = initialUpgradeCount.health,
                        attackUpgrade = initialUpgradeCount.attack,
                        defenseUpgrade = initialUpgradeCount.defense,
                        healthUpgradePlusButtonEnabled = isUpgradeAffordable(initialUpgradeCount.health, 0),
                        healthUpgradeMinusButtonEnabled = false,
                        attackUpgradePlusButtonEnabled = isUpgradeAffordable(initialUpgradeCount.attack, 0),
                        attackUpgradeMinusButtonEnabled = false,
                        defenseUpgradePlusButtonEnabled = isUpgradeAffordable(initialUpgradeCount.defense, 0),
                        defenseUpgradeMinusButtonEnabled = false,
                        isAnyUpgradeSelected = isAnyUpgradeSelected(initialUpgradeCount.health, initialUpgradeCount.attack, initialUpgradeCount.defense),
                        gold = gold,
                        goldCost = 0,
                    )
                }
            }

        }

    }
    fun saveUpgrades(context: Context) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[intPreferencesKey(HEALTH_KEY)] = Settings.healthBaseValue + (_statsUpgradeUiState.value.healthUpgrade * HEALTH_UPGRADE_MULTIPLIER)
                preferences[intPreferencesKey(HEALTH_UPGRADE_COUNT_KEY)] = _statsUpgradeUiState.value.healthUpgrade
                preferences[intPreferencesKey(ATTACK_KEY)] = Settings.attackBaseValue + _statsUpgradeUiState.value.attackUpgrade
                preferences[intPreferencesKey(ATTACK_UPGRADE_COUNT_KEY)] = _statsUpgradeUiState.value.attackUpgrade
                preferences[intPreferencesKey(DEFENSE_KEY)] = Settings.defenseBaseValue + _statsUpgradeUiState.value.defenseUpgrade
                preferences[intPreferencesKey(DEFENSE_UPGRADE_COUNT_KEY)] = _statsUpgradeUiState.value.defenseUpgrade
                preferences[intPreferencesKey(GOLD_KEY)] = _statsUpgradeUiState.value.gold-_statsUpgradeUiState.value.goldCost
            }
        }
    }

    private fun isAnyUpgradeSelected(healthUpgrade: Int, attackUpgrade: Int, defenseUpgrade: Int): Boolean = isUpgradeSelected(healthUpgrade, initialUpgradeCount.health)
            || isUpgradeSelected(attackUpgrade, initialUpgradeCount.attack)
            || isUpgradeSelected(defenseUpgrade, initialUpgradeCount.defense)


    private fun calcCost(upgradeCount: Int): Int = ((upgradeCount+1).toDouble().pow(2.0) * COST_PER_UPGRADE).toInt()
    fun setupMediaPlayer(context: Context) {
        if (!this::mediaPlayer.isInitialized){
            mediaPlayer = MediaPlayer.create(context, R.raw.menu)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        }
    }

    fun startMediaPlayer() {
        mediaPlayer.start()
    }

    fun pauseMediaPlayer() {
        mediaPlayer.pause()
    }

    fun releaseMediaPlayer() {
        mediaPlayer.release()
    }

    fun returnToMain(onNavigate: (Int) -> Unit, destination: Int, context: Context) {
        saveUpgrades(context)
        onNavigate(destination)
    }
}