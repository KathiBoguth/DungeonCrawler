package com.example.dungeoncrawler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentMainBinding
import com.example.dungeoncrawler.mainMenu.MainMenuScreen
import com.example.dungeoncrawler.viewmodel.MenuViewModel

class MainFragment : Fragment() {

    private val menuViewModel: MenuViewModel by activityViewModels()

    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // val fragmentBinding = FragmentMainBinding.inflate(inflater, container, false)
        // binding = fragmentBinding

        return ComposeView(requireContext()).apply {
            setContent {
                MainMenuScreen(onNavigate = { dest -> findNavController().navigate(dest) })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            mainFragment = this@MainFragment
        }
        //menuViewModel.setupMediaPlayer(requireContext())
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
        this.findNavController().navigate(R.id.action_mainFragment_to_gameView)
    }

    fun upgradeStats() {
        this.findNavController().navigate(R.id.action_mainFragment_to_statsUpgradeFragment)
    }
}