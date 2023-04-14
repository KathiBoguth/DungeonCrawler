package com.example.dungeoncrawler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentStatsUpgradeBinding

class StatsUpgradeFragment: Fragment() {

    //TODO: negative money?

    val statsViewModel: StatsViewModel by activityViewModels()

    private var binding: FragmentStatsUpgradeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentStatsUpgradeBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            upgradeStatsFragment = this@StatsUpgradeFragment
        }
    }

    override fun onResume() {
        super.onResume()
        statsViewModel.loadStats(requireContext())
        updateInitialValues()
        updateButtonsEnabled()
        statsViewModel.startMediaPlayer()
    }

    override fun onPause() {
        super.onPause()
        statsViewModel.pauseMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        statsViewModel.releaseMediaPlayer()
    }

    fun onHealthPlusButtonClicked() {
        statsViewModel.onHealthPlusButtonClicked()
        binding?.healthUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, statsViewModel.healthUpgrade*statsViewModel.getHealthUpgradeMultiplier())
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onHealthMinusButtonClicked() {
        statsViewModel.onHealthMinusButtonClicked()
        binding?.healthUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, statsViewModel.healthUpgrade*statsViewModel.getHealthUpgradeMultiplier())
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onAttackPlusButtonClicked() {
        statsViewModel.onAttackPlusButtonClicked()
        binding?.attackUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, statsViewModel.attackUpgrade)
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onAttackMinusButtonClicked() {
        statsViewModel.onAttackMinusButtonClicked()
        binding?.attackUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, statsViewModel.attackUpgrade)
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onDefensePlusButtonClicked() {
        statsViewModel.onDefensePlusButtonClicked()
        binding?.defenseUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, statsViewModel.defenseUpgrade)
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onDefenseMinusButtonClicked() {
        statsViewModel.onDefenseMinusButtonClicked()
        binding?.defenseUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, statsViewModel.defenseUpgrade)
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun reset() {
        statsViewModel.reset()
        binding?.healthUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, statsViewModel.getCurrentHealthUpgrade()*statsViewModel.getHealthUpgradeMultiplier())
        )
        binding?.attackUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, statsViewModel.getCurrentAttackUpgrade())
        )
        binding?.defenseUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, statsViewModel.getCurrentDefenseUpgrade())
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun returnToMain() {
        statsViewModel.saveUpgrades(requireContext())
        this.findNavController().navigate(R.id.action_statsUpgradeFragment_to_mainFragment)
    }

    private fun updateInitialValues() {
        binding?.currentHealth?.text = String.format(
            resources.getString(R.string.health, statsViewModel.initialData.health)
        )
        binding?.currentAttack?.text = String.format(
            resources.getString(R.string.attack, statsViewModel.initialData.attack)
        )
        binding?.currentDefense?.text = String.format(
            resources.getString(R.string.defense, statsViewModel.initialData.defense)
        )
        binding?.goldAvailable?.text = String.format(
            resources.getString(R.string.gold, statsViewModel.initialData.gold)
        )
    }

    private fun updateGoldCost() {
        binding?.goldUpdate?.text = String.format(
            resources.getString(R.string.minus, statsViewModel.goldCost)
        )
    }

    private fun updateButtonsEnabled() {
        binding?.healthPlus?.isEnabled = true
        binding?.attackPlus?.isEnabled = true
        binding?.defensePlus?.isEnabled = true

        if (!statsViewModel.isHealthUpgradeAffordable()) {
            binding?.healthPlus?.isEnabled = false
        }
        if (!statsViewModel.isAttackUpgradeAffordable()) {
            binding?.attackPlus?.isEnabled = false
        }
        if (!statsViewModel.isDefenseUpgradeAffordable()) {
            binding?.defensePlus?.isEnabled = false
        }

        binding?.healthMinus?.isEnabled = false
        binding?.attackMinus?.isEnabled = false
        binding?.defenseMinus?.isEnabled = false

        if (statsViewModel.isHealthUpgradeSelected()) {
            binding?.healthMinus?.isEnabled = true
        }
        if (statsViewModel.isAttackUpgradeSelected()) {
            binding?.attackMinus?.isEnabled = true
        }
        if (statsViewModel.isDefenseUpgradeSelected()) {
            binding?.defenseMinus?.isEnabled = true
        }
        if (statsViewModel.isUpgradeSelected()){
            binding?.returnToMain?.text = String.format(
                resources.getString(R.string.applyAndReturn)
            )

        }
    }
}