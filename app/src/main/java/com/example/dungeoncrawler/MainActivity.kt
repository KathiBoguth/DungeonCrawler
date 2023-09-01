package com.example.dungeoncrawler

import VictoryScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dungeoncrawler.screen.gamescreen.GameScreen
import com.example.dungeoncrawler.screen.mainMenu.MainMenuScreen
import com.example.dungeoncrawler.screen.mainMenu.UpgradeStatsScreen
import com.example.dungeoncrawler.viewmodel.ComposableGameViewModel
import com.example.dungeoncrawler.viewmodel.MenuViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "mainMenu") {
                val menuViewModel: MenuViewModel by viewModels()
                val gameViewModel: ComposableGameViewModel by viewModels()
                composable("mainMenu") {
                    MainMenuScreen(
                        onUpgradeStatsClicked = { navController.navigate("upgradeStats") },
                        onStartGameClicked = { navController.navigate("gameScreen") },
                        menuViewModel = menuViewModel
                    )
                }
                composable("upgradeStats") {
                    UpgradeStatsScreen(
                        {
                            navController.navigate("mainMenu")
                        },
                        menuViewModel
                    )
                }
                composable("gameScreen") {
                    GameScreen(
                        gameViewModel = gameViewModel,
                        onGameOver = { navController.navigate("gameOverScreen") },
                        onVictory = { navController.navigate("victoryScreen") }
                    )
                }
                composable("gameOverScreen") {
                    GameOverScreen(
                        onRestartClicked = { navController.navigate("gameScreen") },
                        onUpgradeStatsClicked = { navController.navigate("upgradeStats") },
                        menuViewModel
                    )
                }
                composable("victoryScreen") {
                    VictoryScreen(
                        onRestartClicked = { navController.navigate("gameScreen") },
                        onUpgradeStatsClicked = { navController.navigate("upgradeStats") },
                        menuViewModel
                    )
                }
            }
        }
    }
}