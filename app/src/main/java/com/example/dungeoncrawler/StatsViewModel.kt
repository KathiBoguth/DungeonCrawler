package com.example.dungeoncrawler

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import kotlin.math.max
import kotlin.math.pow

class StatsViewModel: ViewModel() {

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

    var healthUpgrade = initialUpgradeCount.health
    var attackUpgrade = initialUpgradeCount.attack
    var defenseUpgrade = initialUpgradeCount.defense
    var gold = initialData.gold
    var goldCost = 0

    private lateinit var mediaPlayer: MediaPlayer

    fun onHealthPlusButtonClicked() {
        val cost = calcCost(healthUpgrade)
        if (cost < gold-goldCost) {
            goldCost += cost
            healthUpgrade += 1
        }
    }

    fun onHealthMinusButtonClicked() {
        if (healthUpgrade > 0) {
            val cost = calcCost(max(0,healthUpgrade-1))
            goldCost -= cost
            healthUpgrade -= 1
        }
    }

    fun onAttackPlusButtonClicked() {
        val cost = calcCost(attackUpgrade)
        if (cost <= gold-goldCost) {
            goldCost += cost
            attackUpgrade += 1
        }
    }

    fun onAttackMinusButtonClicked() {
        if (attackUpgrade > 0) {
            val cost = calcCost(max(0, attackUpgrade-1))
            goldCost -= cost
            attackUpgrade -= 1
        }
    }

    fun onDefensePlusButtonClicked() {
        val cost = calcCost(defenseUpgrade)
        if (cost <= gold-goldCost) {
            goldCost += cost
            defenseUpgrade += 1
        }
    }

    fun onDefenseMinusButtonClicked() {
        if (defenseUpgrade > 0) {
            val cost = calcCost(max(0, defenseUpgrade-1))
            goldCost -= cost
            defenseUpgrade -= 1
        }
    }

    fun isHealthUpgradeAffordable(): Boolean {
        val cost = calcCost(healthUpgrade)
        return cost <= gold-goldCost
    }

    fun isHealthUpgradeSelected(): Boolean =
        healthUpgrade > initialUpgradeCount.health

    fun isAttackUpgradeSelected(): Boolean =
        attackUpgrade > initialUpgradeCount.attack

    fun isDefenseUpgradeSelected(): Boolean =
        defenseUpgrade > initialUpgradeCount.defense


    fun isAttackUpgradeAffordable(): Boolean {
        val cost = calcCost(attackUpgrade)
        return cost <= gold-goldCost
    }

    fun isDefenseUpgradeAffordable(): Boolean {
        val cost = calcCost(defenseUpgrade)
        return cost <= gold-goldCost
    }

    fun reset() {
        healthUpgrade = initialUpgradeCount.health
        attackUpgrade = initialUpgradeCount.attack
        defenseUpgrade = initialUpgradeCount.defense
        goldCost = 0

    }

    fun loadStats(context: Context) {
        val stats: SharedPreferences = context.getSharedPreferences(SAVED_STATS_KEY, Context.MODE_PRIVATE)
        val health = stats.getInt(HEALTH_KEY, Settings.healthBaseValue)
        val healthUpgradeCount = stats.getInt(HEALTH_UPGRADE_COUNT_KEY, 0)
        val attack = stats.getInt(ATTACK_KEY, Settings.attackBaseValue)
        val attackUpgradeCount = stats.getInt(ATTACK_UPGRADE_COUNT_KEY, 0)
        val defense = stats.getInt(DEFENSE_KEY, Settings.defenseBaseValue)
        val defenseUpgradeCount = stats.getInt(DEFENSE_UPGRADE_COUNT_KEY, 0)
        gold = stats.getInt(GOLD_KEY, 0)

        initialData = CharaStats(health, attack, defense, gold)
        initialUpgradeCount = CharaStats(healthUpgradeCount, attackUpgradeCount, defenseUpgradeCount, 0)
    }
    fun saveUpgrades(context: Context) {
        val stats: SharedPreferences = context.getSharedPreferences(SAVED_STATS_KEY, Context.MODE_PRIVATE)
        with(stats.edit()){
            putInt(HEALTH_KEY, Settings.healthBaseValue + (healthUpgrade * HEALTH_UPGRADE_MULTIPLIER))
            putInt(HEALTH_UPGRADE_COUNT_KEY, healthUpgrade)
            putInt(ATTACK_KEY, Settings.attackBaseValue + attackUpgrade)
            putInt(ATTACK_UPGRADE_COUNT_KEY, attackUpgrade)
            putInt(DEFENSE_KEY, Settings.defenseBaseValue + defenseUpgrade)
            putInt(DEFENSE_UPGRADE_COUNT_KEY, defenseUpgrade)
            putInt(GOLD_KEY, gold-goldCost)
            apply()
        }
    }

    fun isUpgradeSelected(): Boolean = isHealthUpgradeSelected()
            || isAttackUpgradeSelected()
            || isDefenseUpgradeSelected()

    fun getHealthUpgradeMultiplier() : Int = HEALTH_UPGRADE_MULTIPLIER

    fun getCurrentHealthUpgrade(): Int = healthUpgrade - initialUpgradeCount.health

    fun getCurrentAttackUpgrade(): Int = attackUpgrade - initialUpgradeCount.attack

    fun getCurrentDefenseUpgrade(): Int = defenseUpgrade - initialUpgradeCount.defense


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
}