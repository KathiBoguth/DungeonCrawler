package com.example.dungeoncrawler.viewmodel

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeoncrawler.KilledBy
import com.example.dungeoncrawler.data.CharaStats
import com.example.dungeoncrawler.data.DataStoreData
import com.example.dungeoncrawler.data.StatsUpgradeUiState
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.service.DataStoreManager
import com.example.dungeoncrawler.service.MediaPlayerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

val Context.dataStore by preferencesDataStore(
    name = MenuViewModel.SAVED_STATS_KEY
)
class MenuViewModel : ViewModel() {

    companion object {
        const val HEALTH_UPGRADE_MULTIPLIER = 5
        const val COST_PER_UPGRADE = 50
        const val SAVED_STATS_KEY = "savedStats"
    }

    private var dataStoreManager: DataStoreManager? = null

    lateinit var mediaPlayerService: MediaPlayerService

    fun initDataStoreManager(newManager: DataStoreManager) {
        dataStoreManager = newManager
    }

    fun initMediaPlayerService(newPlayerService: MediaPlayerService) {
        mediaPlayerService = newPlayerService
    }

    private var initialData = CharaStats(0, 0, 0, 0)
    private var initialUpgradeCount = CharaStats(0, 0, 0, 0)

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

    var killedBy = KilledBy(EnemyEnum.SLIME)

    fun onHealthPlusButtonClicked() {
        val cost = calcCost(_statsUpgradeUiState.value.healthUpgrade + initialUpgradeCount.health)
        if (cost < _statsUpgradeUiState.value.gold - _statsUpgradeUiState.value.goldCost) {
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost + cost
                val newHealthUpgrade = it.healthUpgrade + 1
                it.copy(
                    goldCost = newGoldCost,
                    healthUpgrade = newHealthUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(
                        newHealthUpgrade,
                        initialUpgradeCount.health,
                        newGoldCost
                    ),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.attackUpgrade,
                        initialUpgradeCount.attack,
                        newGoldCost
                    ),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.defenseUpgrade,
                        initialUpgradeCount.defense,
                        newGoldCost
                    ),
                    healthUpgradeMinusButtonEnabled = isUpgradeSelected(newHealthUpgrade, 0),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(
                        newHealthUpgrade + initialUpgradeCount.health,
                        it.attackUpgrade,
                        it.defenseUpgrade
                    )
                )
            }
        }
    }

    fun onHealthMinusButtonClicked() {
        if (_statsUpgradeUiState.value.healthUpgrade > 0) {
            val cost = calcCost(max(0,_statsUpgradeUiState.value.healthUpgrade-1+ initialUpgradeCount.health))
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost-cost
                val newHealthUpgrade = it.healthUpgrade-1
                it.copy(
                    goldCost = newGoldCost,
                    healthUpgrade = newHealthUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(
                        newHealthUpgrade,
                        initialUpgradeCount.health,
                        newGoldCost
                    ),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.attackUpgrade,
                        initialUpgradeCount.attack,
                        newGoldCost
                    ),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.defenseUpgrade,
                        initialUpgradeCount.defense,
                        newGoldCost
                    ),
                    healthUpgradeMinusButtonEnabled = isUpgradeSelected(newHealthUpgrade, 0),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(
                        newHealthUpgrade + initialUpgradeCount.health,
                        it.attackUpgrade,
                        it.defenseUpgrade
                    )
                )
            }
        }
    }

    fun onAttackPlusButtonClicked() {
        val cost = calcCost(_statsUpgradeUiState.value.attackUpgrade+initialUpgradeCount.attack)
        if (cost <= _statsUpgradeUiState.value.gold-_statsUpgradeUiState.value.goldCost) {
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost+cost
                val newAttackUpgrade = it.attackUpgrade+1
                it.copy(
                    goldCost = newGoldCost,
                    attackUpgrade = newAttackUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.healthUpgrade,
                        initialUpgradeCount.health,
                        newGoldCost
                    ),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(
                        newAttackUpgrade,
                        initialUpgradeCount.attack,
                        newGoldCost
                    ),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.defenseUpgrade,
                        initialUpgradeCount.defense,
                        newGoldCost
                    ),
                    attackUpgradeMinusButtonEnabled = isUpgradeSelected(newAttackUpgrade, 0),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(
                        it.healthUpgrade,
                        newAttackUpgrade + initialUpgradeCount.attack,
                        it.defenseUpgrade
                    )
                )
            }
        }
    }

    fun onAttackMinusButtonClicked() {
        if (_statsUpgradeUiState.value.attackUpgrade > 0) {
            val cost = calcCost(max(0, _statsUpgradeUiState.value.attackUpgrade + initialUpgradeCount.attack-1))
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost-cost
                val newAttackUpgrade = it.attackUpgrade-1
                it.copy(
                    goldCost = newGoldCost,
                    attackUpgrade = newAttackUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.healthUpgrade,
                        initialUpgradeCount.health,
                        newGoldCost
                    ),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(
                        newAttackUpgrade,
                        initialUpgradeCount.attack,
                        newGoldCost
                    ),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.defenseUpgrade,
                        initialUpgradeCount.defense,
                        newGoldCost
                    ),
                    attackUpgradeMinusButtonEnabled = isUpgradeSelected(newAttackUpgrade, 0),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(
                        it.healthUpgrade,
                        newAttackUpgrade + initialUpgradeCount.attack,
                        it.defenseUpgrade
                    )
                )
            }
        }
    }

    fun onDefensePlusButtonClicked() {
        val cost = calcCost(_statsUpgradeUiState.value.defenseUpgrade + initialUpgradeCount.defense)
        if (cost <= _statsUpgradeUiState.value.gold-_statsUpgradeUiState.value.goldCost) {
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost+cost
                val newDefenseUpgrade = it.defenseUpgrade+1
                it.copy(
                    goldCost = newGoldCost,
                    defenseUpgrade = newDefenseUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.healthUpgrade,
                        initialUpgradeCount.health,
                        newGoldCost
                    ),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.attackUpgrade,
                        initialUpgradeCount.attack,
                        newGoldCost
                    ),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(
                        newDefenseUpgrade,
                        initialUpgradeCount.defense,
                        newGoldCost
                    ),
                    defenseUpgradeMinusButtonEnabled = isUpgradeSelected(newDefenseUpgrade, 0),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(
                        it.healthUpgrade,
                        it.attackUpgrade,
                        newDefenseUpgrade + initialUpgradeCount.defense
                    )
                )
            }
        }
    }

    fun onDefenseMinusButtonClicked() {
        if (_statsUpgradeUiState.value.defenseUpgrade > 0) {
            val cost = calcCost(max(0, _statsUpgradeUiState.value.defenseUpgrade-1+initialUpgradeCount.defense))
            _statsUpgradeUiState.update {
                val newGoldCost = it.goldCost-cost
                val newDefenseUpgrade = it.defenseUpgrade-1
                it.copy(
                    goldCost = newGoldCost,
                    defenseUpgrade = newDefenseUpgrade,
                    healthUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.healthUpgrade,
                        initialUpgradeCount.health,
                        newGoldCost
                    ),
                    attackUpgradePlusButtonEnabled = isUpgradeAffordable(
                        it.attackUpgrade,
                        initialUpgradeCount.attack,
                        newGoldCost
                    ),
                    defenseUpgradePlusButtonEnabled = isUpgradeAffordable(
                        newDefenseUpgrade,
                        initialUpgradeCount.defense,
                        newGoldCost
                    ),
                    defenseUpgradeMinusButtonEnabled = isUpgradeSelected(newDefenseUpgrade, 0),
                    isAnyUpgradeSelected = isAnyUpgradeSelected(
                        it.healthUpgrade,
                        it.attackUpgrade,
                        newDefenseUpgrade + initialUpgradeCount.defense
                    )
                )
            }
        }
    }

    private fun isUpgradeAffordable(upgradeCount: Int, initialUpgrade: Int, goldCost: Int): Boolean {
        val cost = calcCost(upgradeCount+initialUpgrade)
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
                healthUpgradePlusButtonEnabled = isUpgradeAffordable(0, initialUpgradeCount.health, 0),
                healthUpgradeMinusButtonEnabled = false,
                attackUpgradePlusButtonEnabled = isUpgradeAffordable(0, initialUpgradeCount.attack, 0),
                attackUpgradeMinusButtonEnabled = false,
                defenseUpgradePlusButtonEnabled = isUpgradeAffordable(0, initialUpgradeCount.defense, 0),
                defenseUpgradeMinusButtonEnabled = false,
                isAnyUpgradeSelected = false,
                goldCost = 0
            )
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            dataStoreManager?.getDataFromDataStoreUpgradeScreen()?.collect {
                initialData = CharaStats(it.health, it.attack, it.defense, it.gold)
                gold = it.gold
                initialUpgradeCount = CharaStats(
                    it.healthUpgradeCount,
                    it.attackUpgradeCount,
                    it.defenseUpgradeCount,
                    0
                )
                _statsUpgradeUiState.update {
                    StatsUpgradeUiState(
                        initialData = initialData,
                        healthUpgrade = 0,
                        attackUpgrade = 0,
                        defenseUpgrade = 0,
                        healthUpgradePlusButtonEnabled = isUpgradeAffordable(
                            0,
                            initialUpgradeCount.health,
                            0
                        ),
                        healthUpgradeMinusButtonEnabled = false,
                        attackUpgradePlusButtonEnabled = isUpgradeAffordable(0, initialUpgradeCount.attack, 0),
                        attackUpgradeMinusButtonEnabled = false,
                        defenseUpgradePlusButtonEnabled = isUpgradeAffordable(0, initialUpgradeCount.defense, 0),
                        defenseUpgradeMinusButtonEnabled = false,
                        isAnyUpgradeSelected = isAnyUpgradeSelected(initialUpgradeCount.health, initialUpgradeCount.attack, initialUpgradeCount.defense),
                        gold = gold,
                        goldCost = 0,
                    )
                }
            }
        }
    }

    private fun saveUpgrades() {
        viewModelScope.launch {
            val dataStoreData = DataStoreData(
                0, 0, 0,
                gold = _statsUpgradeUiState.value.gold - _statsUpgradeUiState.value.goldCost,
                healthUpgradeCount = _statsUpgradeUiState.value.healthUpgrade + initialUpgradeCount.health,
                attackUpgradeCount = _statsUpgradeUiState.value.attackUpgrade + initialUpgradeCount.attack,
                defenseUpgradeCount = _statsUpgradeUiState.value.defenseUpgrade + initialUpgradeCount.defense
            )
            dataStoreManager?.saveToDataStore(dataStoreData)
        }
    }

    private fun isAnyUpgradeSelected(healthUpgrade: Int, attackUpgrade: Int, defenseUpgrade: Int): Boolean = isUpgradeSelected(healthUpgrade, initialUpgradeCount.health)
            || isUpgradeSelected(attackUpgrade, initialUpgradeCount.attack)
            || isUpgradeSelected(defenseUpgrade, initialUpgradeCount.defense)


    private fun calcCost(upgradeCount: Int): Int = ((upgradeCount+1).toDouble().pow(2.0) * COST_PER_UPGRADE).toInt()

    fun returnToMain(onNavigateBack: () -> Unit) {
        saveUpgrades()
        onNavigateBack()
    }

    fun getHighscore(): Flow<Int> = dataStoreManager?.getHighscoreData() ?: flowOf(0)
}