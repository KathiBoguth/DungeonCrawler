package com.example.dungeoncrawler.screen.gamescreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.data.CharaState
import com.example.dungeoncrawler.data.EnemyState
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.CoordinatesDp
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.ground.BackgroundComposable
import com.example.dungeoncrawler.service.DataStoreManager
import com.example.dungeoncrawler.viewmodel.ComposableGameViewModel


val triangleShape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(0f, size.height / 2f)
}

@Composable
fun GameScreen(gameViewModel: ComposableGameViewModel = viewModel(), onNavigate: (Int) -> Unit) {
    val charaState by gameViewModel.charaStateFlow.collectAsState()
    val enemiesState by gameViewModel.enemiesStateFlow.collectAsState()
    LaunchedEffect(Unit) {
        gameViewModel.reset()
    }

    val endGame by gameViewModel.endGame.collectAsState()
    endGame?.let { onEndGame(it, onNavigate) }

    gameViewModel.initDataStoreManager(DataStoreManager(LocalContext.current)) // TODO: not a singleton

    GameScreen(
        charaState,
        enemiesState,
        gameViewModel::interact,
        gameViewModel::moveUp,
        gameViewModel::moveDown,
        gameViewModel::moveLeft,
        gameViewModel::moveRight,
        0 // TODO: levelCount
    )
}

@Composable
fun GameScreen(
    charaState: CharaState,
    enemiesState: List<EnemyState>,
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

    val backgroundPosition by remember(key1 = charaState.position, key2 = backgroundOrigPosition) {
        val moveLength = Settings.moveLength
        val xPosBackground = backgroundOrigPosition.x.minus((charaState.position.x * moveLength).dp)
        val yPosBackground = backgroundOrigPosition.y.minus((charaState.position.y * moveLength).dp)
        return@remember mutableStateOf(CoordinatesDp(xPosBackground, yPosBackground))
    }

    BackgroundComposable(backgroundPosition, enemiesState)

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
            levelCount
        )

    }

}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun GamePreview() {
    GameScreen(charaState = CharaState(
        direction = Direction.DOWN, nudge = false, jump = false,
        position = Coordinates(0, 0), flashRed = false, gold = 0, health = 0
    ),
        enemiesState = listOf(
            EnemyState(
                "",
                false,
                false,
                Direction.DOWN,
                Coordinates(-2, -5),
                EnemyEnum.SLIME,
                flashRed = false
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

private fun onEndGame(victory: Boolean, onNavigate: (Int) -> Unit) {

//            removeEnemyObservers()
//            hideAllEnemies()
//            saveGold()
            //if (findNavController(view).currentDestination?.id == R.id.gameView) {
                if (victory) {
                    onNavigate(R.id.action_gameView_to_victoryView)
                } else {
                    onNavigate(R.id.action_gameView_to_gameOverView)
                }
                //this.cancel()
            //}
}