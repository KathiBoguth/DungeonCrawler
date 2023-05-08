package com.example.dungeoncrawler.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeoncrawler.CharaStats
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.StatsUpgradeUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(
    name = MenuViewModel.SAVED_STATS_KEY
)
class MenuViewModel @Inject constructor( app: Application
): AndroidViewModel(app) {

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

    // TODO: remove when Fragment deleted
    var healthUpgrade = initialUpgradeCount.health
    var attackUpgrade = initialUpgradeCount.attack
    var defenseUpgrade = initialUpgradeCount.defense
    var gold = initialData.gold
    var goldCost = 0

    var healthUpgradeButtonEnabled = false
    var attackUpgradeButtonEnabled = false
    var defenseUpgradeButtonEnabled = false
    // End of TODO: remove when Fragment is deleted

    // TODO should be val, check how this is working
    private var statsUpgradeUiState = StatsUpgradeUiState()

    val uiState: StateFlow<StatsUpgradeUiState> = flowOf(statsUpgradeUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = statsUpgradeUiState
        )

    private lateinit var mediaPlayer: MediaPlayer

    init {
        // TODO: music starts wrongly/multiple times etc
        setupMediaPlayer(getApplication<Application>().applicationContext)
    }

    fun onHealthPlusButtonClicked() {
        val cost = calcCost(statsUpgradeUiState.healthUpgrade)
        if (cost < statsUpgradeUiState.gold-statsUpgradeUiState.goldCost) {
            uiState.value.goldCost += cost
            uiState.value.healthUpgrade += 1
        }
    }

    fun onHealthMinusButtonClicked() {
        if (statsUpgradeUiState.healthUpgrade > 0) {
            val cost = calcCost(max(0,statsUpgradeUiState.healthUpgrade-1))
            uiState.value.goldCost -= cost
            uiState.value.healthUpgrade -= 1
        }
    }

    fun onAttackPlusButtonClicked() {
        val cost = calcCost(statsUpgradeUiState.attackUpgrade)
        if (cost <= statsUpgradeUiState.gold-statsUpgradeUiState.goldCost) {
            uiState.value.goldCost += cost
            uiState.value.attackUpgrade += 1
        }
    }

    fun onAttackMinusButtonClicked() {
        if (statsUpgradeUiState.attackUpgrade > 0) {
            val cost = calcCost(max(0, statsUpgradeUiState.attackUpgrade-1))
            uiState.value.goldCost -= cost
            uiState.value.attackUpgrade -= 1
        }
    }

    fun onDefensePlusButtonClicked() {
        val cost = calcCost(statsUpgradeUiState.defenseUpgrade)
        if (cost <= statsUpgradeUiState.gold-statsUpgradeUiState.goldCost) {
            uiState.value.goldCost += cost
            uiState.value.defenseUpgrade += 1
        }
    }

    fun onDefenseMinusButtonClicked() {
        if (statsUpgradeUiState.defenseUpgrade > 0) {
            val cost = calcCost(max(0, statsUpgradeUiState.defenseUpgrade-1))
            uiState.value.goldCost -= cost
            uiState.value.defenseUpgrade -= 1
        }
    }

    fun isHealthUpgradeAffordable(): Boolean {
        val cost = calcCost(statsUpgradeUiState.healthUpgrade)
        return cost <= statsUpgradeUiState.gold-statsUpgradeUiState.goldCost
    }

    fun isHealthUpgradeSelected(): Boolean =
        statsUpgradeUiState.healthUpgrade > initialUpgradeCount.health

    fun isAttackUpgradeSelected(): Boolean =
        statsUpgradeUiState.attackUpgrade > initialUpgradeCount.attack

    fun isDefenseUpgradeSelected(): Boolean =
        statsUpgradeUiState.defenseUpgrade > initialUpgradeCount.defense


    fun isAttackUpgradeAffordable(): Boolean {
        val cost = calcCost(statsUpgradeUiState.attackUpgrade)
        return cost <= statsUpgradeUiState.gold-statsUpgradeUiState.goldCost
    }

    fun isDefenseUpgradeAffordable(): Boolean {
        val cost = calcCost(statsUpgradeUiState.defenseUpgrade)
        return cost <= statsUpgradeUiState.gold-statsUpgradeUiState.goldCost
    }

    fun reset() {
        uiState.value.healthUpgrade = initialUpgradeCount.health
        uiState.value.attackUpgrade = initialUpgradeCount.attack
        uiState.value.defenseUpgrade = initialUpgradeCount.defense
        uiState.value.goldCost = 0

    }

    fun loadStats() {

        viewModelScope.launch {
            getApplication<Application>().dataStore.data.collect{preferences ->
                val health = preferences[intPreferencesKey(HEALTH_KEY)] ?: Settings.healthBaseValue
                val healthUpgradeCount = preferences[intPreferencesKey(HEALTH_UPGRADE_COUNT_KEY)] ?: 0
                val attack = preferences[intPreferencesKey(ATTACK_KEY)] ?: Settings.attackBaseValue
                val attackUpgradeCount = preferences[intPreferencesKey(ATTACK_UPGRADE_COUNT_KEY)] ?: 0
                val defense = preferences[intPreferencesKey(DEFENSE_KEY)] ?: Settings.defenseBaseValue
                val defenseUpgradeCount = preferences[intPreferencesKey(DEFENSE_UPGRADE_COUNT_KEY)] ?: 0
                val gold = preferences[intPreferencesKey(GOLD_KEY)] ?: 0

                initialData = CharaStats(health, attack, defense, gold)
                initialUpgradeCount = CharaStats(healthUpgradeCount, attackUpgradeCount, defenseUpgradeCount, 0)
                // TODO: get this and fix this
                statsUpgradeUiState = StatsUpgradeUiState(
                    initialData = initialData,
                    healthUpgrade = initialData.health,
                    attackUpgrade = initialData.attack,
                    defenseUpgrade = initialData.defense,
                    gold = gold,
                    goldCost = 0,
                )
            }

        }

    }
    fun saveUpgrades() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { preferences ->
                preferences[intPreferencesKey(HEALTH_KEY)] = Settings.healthBaseValue + (statsUpgradeUiState.healthUpgrade * HEALTH_UPGRADE_MULTIPLIER)
                preferences[intPreferencesKey(HEALTH_UPGRADE_COUNT_KEY)] = statsUpgradeUiState.healthUpgrade
                preferences[intPreferencesKey(ATTACK_KEY)] = Settings.attackBaseValue + statsUpgradeUiState.attackUpgrade
                preferences[intPreferencesKey(ATTACK_UPGRADE_COUNT_KEY)] = statsUpgradeUiState.attackUpgrade
                preferences[intPreferencesKey(DEFENSE_KEY)] = Settings.defenseBaseValue + statsUpgradeUiState.defenseUpgrade
                preferences[intPreferencesKey(DEFENSE_UPGRADE_COUNT_KEY)] = statsUpgradeUiState.defenseUpgrade
                preferences[intPreferencesKey(GOLD_KEY)] = statsUpgradeUiState.gold-statsUpgradeUiState.goldCost
            }
        }
    }

    fun isUpgradeSelected(): Boolean = isHealthUpgradeSelected()
            || isAttackUpgradeSelected()
            || isDefenseUpgradeSelected()

    fun getHealthUpgradeMultiplier() : Int = HEALTH_UPGRADE_MULTIPLIER

    fun getCurrentHealthUpgrade(): Int = statsUpgradeUiState.healthUpgrade - initialUpgradeCount.health

    fun getCurrentAttackUpgrade(): Int = statsUpgradeUiState.attackUpgrade - initialUpgradeCount.attack

    fun getCurrentDefenseUpgrade(): Int = statsUpgradeUiState.defenseUpgrade - initialUpgradeCount.defense


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

    fun returnToMain(onNavigate: (Int) -> Unit, destination: Int) {
        saveUpgrades()
        onNavigate(destination)
    }

    // TODO use this I guess?
    private fun updateButtonsEnabled() {
        uiState.value.healthUpgradeButtonEnabled = true
        uiState.value.attackUpgradeButtonEnabled = true
        uiState.value.defenseUpgradeButtonEnabled = true

        if (!isHealthUpgradeAffordable()) {
            uiState.value.healthUpgradeButtonEnabled = false
        }
        if (!isAttackUpgradeAffordable()) {
            uiState.value.attackUpgradeButtonEnabled = false
        }
        if (!isDefenseUpgradeAffordable()) {
            uiState.value.defenseUpgradeButtonEnabled = false
        }
    }
}