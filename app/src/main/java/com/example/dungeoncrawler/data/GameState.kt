package com.example.dungeoncrawler.data

sealed class GameState {
    object InitGame : GameState()
    object EndGameOnGameOver : GameState()
    object EndGameOnVictory : GameState()
    object NextLevel: GameState()
    data class NextLevelReady(val levelCount: Int): GameState()
}