package com.example.dungeoncrawler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.screen.mainMenu.MainMenuScreen
import com.example.dungeoncrawler.viewmodel.MenuViewModel

class MainFragment : Fragment() {

    private val menuViewModel: MenuViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                MainMenuScreen(onNavigate = { dest -> findNavController().navigate(dest) }, menuViewModel = menuViewModel )
            }
        }
    }
}