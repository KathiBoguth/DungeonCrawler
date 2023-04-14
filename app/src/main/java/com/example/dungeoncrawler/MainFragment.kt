package com.example.dungeoncrawler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val statsViewModel: StatsViewModel by activityViewModels()

    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentMainBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            mainFragment = this@MainFragment
        }
        statsViewModel.setupMediaPlayer(requireContext())
    }

    override fun onPause() {
        super.onPause()
        statsViewModel.pauseMediaPlayer()
    }

    override fun onResume() {
        super.onResume()
        statsViewModel.startMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        statsViewModel.releaseMediaPlayer()
    }

    fun startGame() {
        statsViewModel.pauseMediaPlayer()
        this.findNavController().navigate(R.id.action_mainFragment_to_gameView)
    }

    fun upgradeStats() {
        this.findNavController().navigate(R.id.action_mainFragment_to_statsUpgradeFragment)
    }
}