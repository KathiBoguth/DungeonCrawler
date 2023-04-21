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

    val menuViewModel: MenuViewModel by activityViewModels()

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
        menuViewModel.loadStats(requireContext())
        updateInitialValues()
        updateButtonsEnabled()
        menuViewModel.startMediaPlayer()
    }

    override fun onPause() {
        super.onPause()
        menuViewModel.pauseMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        menuViewModel.releaseMediaPlayer()
    }

    fun onHealthPlusButtonClicked() {
        menuViewModel.onHealthPlusButtonClicked()
        binding?.healthUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, menuViewModel.healthUpgrade*menuViewModel.getHealthUpgradeMultiplier())
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onHealthMinusButtonClicked() {
        menuViewModel.onHealthMinusButtonClicked()
        binding?.healthUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, menuViewModel.healthUpgrade*menuViewModel.getHealthUpgradeMultiplier())
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onAttackPlusButtonClicked() {
        menuViewModel.onAttackPlusButtonClicked()
        binding?.attackUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, menuViewModel.attackUpgrade)
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onAttackMinusButtonClicked() {
        menuViewModel.onAttackMinusButtonClicked()
        binding?.attackUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, menuViewModel.attackUpgrade)
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onDefensePlusButtonClicked() {
        menuViewModel.onDefensePlusButtonClicked()
        binding?.defenseUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, menuViewModel.defenseUpgrade)
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun onDefenseMinusButtonClicked() {
        menuViewModel.onDefenseMinusButtonClicked()
        binding?.defenseUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, menuViewModel.defenseUpgrade)
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun reset() {
        menuViewModel.reset()
        binding?.healthUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, menuViewModel.getCurrentHealthUpgrade()*menuViewModel.getHealthUpgradeMultiplier())
        )
        binding?.attackUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, menuViewModel.getCurrentAttackUpgrade())
        )
        binding?.defenseUpgrade?.text = String.format(
            resources.getString(R.string.upgrade, menuViewModel.getCurrentDefenseUpgrade())
        )
        updateGoldCost()
        updateButtonsEnabled()
    }

    fun returnToMain() {
        menuViewModel.saveUpgrades(requireContext())
        this.findNavController().navigate(R.id.action_statsUpgradeFragment_to_mainFragment)
    }

    private fun updateInitialValues() {
        binding?.currentHealth?.text = String.format(
            resources.getString(R.string.health, menuViewModel.initialData.health)
        )
        binding?.currentAttack?.text = String.format(
            resources.getString(R.string.attack, menuViewModel.initialData.attack)
        )
        binding?.currentDefense?.text = String.format(
            resources.getString(R.string.defense, menuViewModel.initialData.defense)
        )
        binding?.goldAvailable?.text = String.format(
            resources.getString(R.string.gold, menuViewModel.initialData.gold)
        )
    }

    private fun updateGoldCost() {
        binding?.goldUpdate?.text = String.format(
            resources.getString(R.string.minus, menuViewModel.goldCost)
        )
    }

    private fun updateButtonsEnabled() {
        binding?.healthPlus?.isEnabled = true
        binding?.attackPlus?.isEnabled = true
        binding?.defensePlus?.isEnabled = true

        if (!menuViewModel.isHealthUpgradeAffordable()) {
            binding?.healthPlus?.isEnabled = false
        }
        if (!menuViewModel.isAttackUpgradeAffordable()) {
            binding?.attackPlus?.isEnabled = false
        }
        if (!menuViewModel.isDefenseUpgradeAffordable()) {
            binding?.defensePlus?.isEnabled = false
        }

        binding?.healthMinus?.isEnabled = false
        binding?.attackMinus?.isEnabled = false
        binding?.defenseMinus?.isEnabled = false

        if (menuViewModel.isHealthUpgradeSelected()) {
            binding?.healthMinus?.isEnabled = true
        }
        if (menuViewModel.isAttackUpgradeSelected()) {
            binding?.attackMinus?.isEnabled = true
        }
        if (menuViewModel.isDefenseUpgradeSelected()) {
            binding?.defenseMinus?.isEnabled = true
        }
        if (menuViewModel.isUpgradeSelected()){
            binding?.returnToMain?.text = String.format(
                resources.getString(R.string.applyAndReturn)
            )

        }
    }
}