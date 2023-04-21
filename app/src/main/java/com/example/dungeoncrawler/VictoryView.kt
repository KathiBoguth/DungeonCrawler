package com.example.dungeoncrawler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentVictoryBinding

class VictoryView: Fragment() {

    val gameViewModel: GameViewModel by activityViewModels()
    private val menuViewModel: MenuViewModel by activityViewModels()

    private var binding: FragmentVictoryBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentVictoryBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            victoryFragment = this@VictoryView
        }

        menuViewModel.startMediaPlayer()
    }

    override fun onPause() {
        super.onPause()
        menuViewModel.pauseMediaPlayer()
    }

    override fun onResume() {
        super.onResume()
        menuViewModel.startMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        menuViewModel.releaseMediaPlayer()
    }

    fun startGame() {
        this.findNavController().navigate(R.id.action_victoryView_to_gameView)
        menuViewModel.pauseMediaPlayer()
    }

    fun upgradeStats() {
        this.findNavController().navigate(R.id.action_victoryView_to_statsUpgradeFragment)
    }
}