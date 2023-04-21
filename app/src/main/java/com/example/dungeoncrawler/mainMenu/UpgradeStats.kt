package com.example.dungeoncrawler.mainMenu

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavHostController
import com.example.dungeoncrawler.databinding.FragmentStatsUpgradeBinding

@Composable
fun  UpgradeStats(navController: NavHostController) {
    AndroidViewBinding(FragmentStatsUpgradeBinding::inflate) {
    }
}