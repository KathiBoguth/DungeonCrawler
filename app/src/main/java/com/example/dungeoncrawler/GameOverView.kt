package com.example.dungeoncrawler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentGameOverBinding
import com.example.dungeoncrawler.viewmodel.GameViewModel
import com.example.dungeoncrawler.viewmodel.MenuViewModel

class GameOverView: Fragment() {

    val gameViewModel: GameViewModel by activityViewModels()
    private val menuViewModel: MenuViewModel by activityViewModels()

    private var binding: FragmentGameOverBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentGameOverBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            gameOverFragment = this@GameOverView
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
        menuViewModel.pauseMediaPlayer()
        this.findNavController().navigate(R.id.action_gameOverView_to_gameView)
    }

    fun upgradeStats() {
        this.findNavController().navigate(R.id.action_gameOverView_to_statsUpgradeFragment)
    }
}