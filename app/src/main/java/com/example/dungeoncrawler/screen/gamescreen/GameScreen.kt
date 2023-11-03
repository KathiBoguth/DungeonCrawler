package com.example.dungeoncrawler.screen.gamescreen

import PauseScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.CharaScreenState
import com.example.dungeoncrawler.data.EnemyState
import com.example.dungeoncrawler.data.GameState
import com.example.dungeoncrawler.data.LevelObjectState
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.CoordinatesDp
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.GroundType
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.screen.ground.BackgroundComposable
import com.example.dungeoncrawler.viewmodel.ComposableGameViewModel
import kotlinx.coroutines.delay

val triangleShape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(0f, size.height / 2f)
}

@Composable
fun GameScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    gameViewModel: ComposableGameViewModel = viewModel(),
    onGameOver: () -> Unit,
    onVictory: () -> Unit,
    onGiveUp: () -> Unit
) {
    val charaState by gameViewModel.charaStateFlow.collectAsState()
    val enemiesState = gameViewModel.enemiesStateList
    val objectsState = gameViewModel.objectsStateList
    val fieldLayoutState = gameViewModel.fieldLayoutState.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        gameViewModel.reset(context = context)

    }

    var isVisible by remember {
        mutableStateOf(false)
    }
    var levelCount by remember {
        mutableStateOf(0)
    }

    val gameState by gameViewModel.gameState.collectAsState()
    val pausedState by gameViewModel.gamePaused.collectAsState()
    val highscore by gameViewModel.getHighscore().collectAsState(initial = 0)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                gameViewModel.mediaPlayerService.startMediaPlayerByLevelCount(levelCount)
            } else if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                gameViewModel.mediaPlayerService.pauseMediaPlayers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = gameState) {
        // TODO: maybe add state "loading" and loading screen tp remove the reset()
        when (val state = gameState) {
            is GameState.EndGameOnGameOver -> {
                gameViewModel.reset(context = context)
                onEndGame(
                    onGameOver,
                    gameViewModel.mediaPlayerService::pauseMediaPlayers
                )
            }

            is GameState.EndGameOnVictory -> {
                gameViewModel.reset(context = context)
                onEndGame(
                    onVictory,
                    gameViewModel.mediaPlayerService::pauseMediaPlayers
                )
            }

            is GameState.EndGameOnGiveUp -> {
                gameViewModel.reset(context = context)
                onEndGame(onGiveUp, gameViewModel.mediaPlayerService::pauseMediaPlayers)
            }

            is GameState.InitGame -> {
                levelCount = state.levelCount
            }

            is GameState.NextLevel -> {
                isVisible = false
                delay(500)
                gameViewModel.reset(false, context)
            }

            is GameState.NextLevelReady -> {
                isVisible = true
                levelCount = state.levelCount
                if (levelCount >= Settings.enemiesPerLevel.size) {
                    gameViewModel.mediaPlayerService.pauseMediaPlayerDungeon()
                    gameViewModel.mediaPlayerService.startMediaPlayerBoss()
                }
            }
        }
    }

    AnimatedVisibility(visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (pausedState) {
            PauseScreen(
                onReturnClicked = gameViewModel::resumeGame,
                onGiveUpClicked = gameViewModel::onGiveUp,
                highscore = highscore,
                gameViewModel = gameViewModel
            )
        } else {
            GameScreen(
                charaState,
                enemiesState,
                objectsState,
                gameViewModel::interact,
                gameViewModel::moveUp,
                gameViewModel::moveDown,
                gameViewModel::moveLeft,
                gameViewModel::moveRight,
                gameViewModel::onPause,
                levelCount,
                fieldLayoutState.value
            )
        }
    }
}

@Composable
fun GameScreen(
    charaScreenState: CharaScreenState,
    enemiesState: List<EnemyState>,
    objectsState: List<LevelObjectState>,
    interact: () -> Unit,
    moveUp: () -> Unit,
    moveDown: () -> Unit,
    moveLeft: () -> Unit,
    moveRight: () -> Unit,
    onPause: () -> Unit,
    levelCount: Int,
    fieldLayout: List<List<GroundType>>
) {
    val configuration = LocalConfiguration.current
    val backgroundOrigPosition = if (fieldLayout.isNotEmpty()) {
        val someNumber = -5.3
        val adjustmentX = if (fieldLayout.size >= 10) {
            (someNumber + (0.5 * fieldLayout.size)).times(Settings.moveLength)  //TODO: no idea how to calculate, not connected to density?
        } else {
            -(Settings.moveLength * 0.5)
        }
        val adjustmentY = if (fieldLayout.size >= 10) {
            adjustmentX
        } else {
            -(Settings.moveLength * 1.85)
        }
        CoordinatesDp(
            (configuration.screenWidthDp / 2).dp + adjustmentX.dp, //+ (fieldLayout.size* Settings.moveLength.dp)* (configuration.screenWidthDp / configuration.screenHeightDp),//adjustWidth.dp,
            (configuration.screenWidthDp / 2).dp + adjustmentY.dp// + adjustHeight.dp
        )
    } else {
        CoordinatesDp(0.dp, 0.dp)
    }


    val backgroundPosition by remember(key1 = charaScreenState.position, key2 = levelCount) {
        val moveLength = Settings.moveLength
        val xPosBackground =
            backgroundOrigPosition.x.minus((charaScreenState.position.x * moveLength).dp)
        val yPosBackground =
            backgroundOrigPosition.y.minus((charaScreenState.position.y * moveLength).dp)
        return@remember mutableStateOf(CoordinatesDp(xPosBackground, yPosBackground))
    }

    BackgroundComposable(backgroundPosition, enemiesState, objectsState, levelCount, fieldLayout)

    Box(modifier = Modifier.fillMaxSize()) {
        CharacterScreen(charaScreenState)

        Controls(
            interact,
            moveUp,
            moveDown,
            moveLeft,
            moveRight,
            onPause,
            charaScreenState.gold,
            charaScreenState.health,
            levelCount,
            charaScreenState.weaponId, charaScreenState.cuirassId
        )

    }

}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun GamePreview() {
    val fieldLayout = List(12) { List(12) { GroundType.WATER } }
    GameScreen(
        charaScreenState = CharaScreenState(
            direction = Direction.DOWN,
            nudge = false,
            jump = false,
            position = Coordinates(0, 0),
            flashRed = false,
            gold = 0,
            health = 0,
            weaponId = "",
            cuirassId = "",
            fixated = false
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
                loadsAttack = false,
                healthPercentage = 1.0
            )
        ),
        objectsState = listOf(
            LevelObjectState(
                "",
                LevelObjectType.TREASURE,
                Coordinates(3, 3),
                Direction.DOWN
            )
        ),
        {}, {}, {}, {}, {}, {}, levelCount = 0, fieldLayout
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