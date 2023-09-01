package com.example.dungeoncrawler.screen.gamescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.CharaState
import com.example.dungeoncrawler.data.EnemyState
import com.example.dungeoncrawler.data.GameState
import com.example.dungeoncrawler.data.LevelObjectState
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.CoordinatesDp
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.screen.ground.BackgroundComposable
import com.example.dungeoncrawler.viewmodel.ComposableGameViewModel

val triangleShape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(0f, size.height / 2f)
}

@Composable
fun GameScreen(
    gameViewModel: ComposableGameViewModel = viewModel(),
    onGameOver: () -> Unit,
    onVictory: () -> Unit
) {
    val charaState by gameViewModel.charaStateFlow.collectAsState()
    val enemiesState = gameViewModel.enemiesStateList
    val objectsState = gameViewModel.objectsStateList

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        gameViewModel.reset()
        gameViewModel.setupMediaPlayer(context)
        gameViewModel.startMediaPlayerDungeon()
    }

    var isVisible by remember {
        mutableStateOf(true)
    }
    var levelCount by remember {
        mutableStateOf(0)
    }


    val gameState by gameViewModel.gameState.collectAsState()

    LaunchedEffect(key1 = gameState) {
        when (val state = gameState) {
            is GameState.EndGameOnGameOver -> {
                gameViewModel.reset()
                onEndGame(
                    onGameOver,
                    gameViewModel::pauseMediaPlayers
                )
            }

            is GameState.EndGameOnVictory -> {
                gameViewModel.reset()
                onEndGame(
                    onVictory,
                    gameViewModel::pauseMediaPlayers
                )
            }

            is GameState.InitGame -> {
                levelCount = state.levelCount
            }

            is GameState.NextLevel -> {
                isVisible = false
            }

            is GameState.NextLevelReady -> {
                isVisible = true
                levelCount = state.levelCount
                if (levelCount > Settings.levelsMax) {
                    gameViewModel.pauseMediaPlayers()
                    gameViewModel.startMediaPlayerBoss()
                }
            }
        }
    }

    AnimatedVisibility(visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        GameScreen(
            charaState,
            enemiesState,
            objectsState,
            gameViewModel::interact,
            gameViewModel::moveUp,
            gameViewModel::moveDown,
            gameViewModel::moveLeft,
            gameViewModel::moveRight,
            levelCount
        )
    }
}

@Composable
fun GameScreen(
    charaState: CharaState,
    enemiesState: List<EnemyState>,
    objectsState: List<LevelObjectState>,
    interact: () -> Unit,
    moveUp: () -> Unit,
    moveDown: () -> Unit,
    moveLeft: () -> Unit,
    moveRight: () -> Unit,
    levelCount: Int
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val adjustWidth =
        ((density.density - 2.55) * 200) //TODO: no idea how to calculate, not connected to density?
    val adjustHeight =
        ((density.density - 2.45) * 200) //TODO: no idea how to calculate, not connected to density?
    val backgroundOrigPosition = CoordinatesDp(
        (configuration.screenWidthDp / 2).dp + adjustWidth.dp,
        (configuration.screenWidthDp / 2).dp + adjustHeight.dp
    )

    val backgroundPosition by remember(key1 = charaState.position, key2 = levelCount) {
        val moveLength = Settings.moveLength
        val xPosBackground = backgroundOrigPosition.x.minus((charaState.position.x * moveLength).dp)
        val yPosBackground = backgroundOrigPosition.y.minus((charaState.position.y * moveLength).dp)
        return@remember mutableStateOf(CoordinatesDp(xPosBackground, yPosBackground))
    }

    BackgroundComposable(backgroundPosition, enemiesState, objectsState, levelCount)

    Box(modifier = Modifier.fillMaxSize()) {
        CharacterScreen(charaState)

        Controls(
            interact,
            moveUp,
            moveDown,
            moveLeft,
            moveRight,
            charaState.gold,
            charaState.health,
            levelCount,
            charaState.weaponId, charaState.cuirassId
        )

    }

}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun GamePreview() {
    GameScreen(charaState = CharaState(
        direction = Direction.DOWN,
        nudge = false,
        jump = false,
        position = Coordinates(0, 0),
        flashRed = false,
        gold = 0,
        health = 0,
        weaponId = "",
        cuirassId = ""
    ),
        enemiesState = listOf(
            EnemyState(
                id = "",
                nudge = false,
                jump = false,
                direction = Direction.DOWN,
                position = Coordinates(-2, -5),
                type = EnemyEnum.SLIME,
                flashRed = false,
                visible = true,
                loadsAttack = false
            )
        ),
        objectsState = listOf(
            LevelObjectState(
                "",
                LevelObjectType.TREASURE,
                Coordinates(3,3)
            )
        ),
        {}, {}, {}, {}, {}, levelCount = 0
    )
}

fun getOffset(jump: Boolean, nudge: Boolean, direction: Direction): Offset {

    if (!jump && !nudge) {
        return Offset(0f, 0f)
    }
    if (jump) {
        return Offset(0f, -Settings.nudgeWidth)
    }

    var deltaX = 0
    var deltaY = 0
    when (direction) {
        Direction.UP -> deltaY = -1
        Direction.DOWN -> deltaY = 1
        Direction.LEFT -> deltaX = -1
        Direction.RIGHT -> deltaX = 1
    }

    return Offset(Settings.nudgeWidth * deltaX, Settings.nudgeWidth * deltaY)
}

private fun onEndGame(onNavigate: () -> Unit, pauseMediaPlayers: () -> Unit) {
    pauseMediaPlayers()
    onNavigate()
}