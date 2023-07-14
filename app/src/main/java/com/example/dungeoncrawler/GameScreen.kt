package com.example.dungeoncrawler

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.CoordinatesDp
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.ground.BackgroundComposable
import com.example.dungeoncrawler.viewmodel.ComposableGameViewModel


val triangleShape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(0f, size.height / 2f)
}

@Composable
fun GameScreen(gameViewModel: ComposableGameViewModel = viewModel()) {
    val charaState by gameViewModel.charaStateFlow.collectAsState()
    val enemiesState by gameViewModel.enemiesStateFlow.collectAsState()
    LaunchedEffect(Unit) {
        gameViewModel.reset()
    }

    GameScreen(
        charaState,
        enemiesState,
        gameViewModel::interact,
        gameViewModel::moveUp,
        gameViewModel::moveDown,
        gameViewModel::moveLeft,
        gameViewModel::moveRight
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
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val adjustWidth = ((density.density - 2.55) * 200) //TODO: no idea how to calculate, not connected to density?
    val adjustHeight = ((density.density - 2.45) * 200) //TODO: no idea how to calculate, not connected to density?
    val backgroundOrigPosition = CoordinatesDp(
        (configuration.screenWidthDp/2).dp + adjustWidth.dp,
        (configuration.screenWidthDp/2).dp + adjustHeight.dp
    )

    val backgroundPosition by remember(key1 = charaState.position, key2 = backgroundOrigPosition) {
        val moveLength = Settings.moveLength
        val xPosBackground = backgroundOrigPosition.x.minus((charaState.position.x*moveLength).dp)
        val yPosBackground = backgroundOrigPosition.y.minus((charaState.position.y*moveLength).dp)
        return@remember mutableStateOf(CoordinatesDp(xPosBackground, yPosBackground))
    }

    BackgroundComposable(backgroundPosition)

    Box(modifier = Modifier.fillMaxSize()) {
        Character(charaState)

        Controls(interact, moveUp, moveDown, moveLeft, moveRight)

        //enemies
        enemiesState.forEach { enemy ->

            Enemy(enemy, backgroundPosition)
        }
    }

}

@Composable
private fun Controls(
    interact: () -> Unit,
    moveUp: () -> Unit,
    moveDown: () -> Unit,
    moveLeft: () -> Unit,
    moveRight: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(76.dp, 112.dp),
        contentAlignment = BottomEnd
    ) {
        FloatingActionButton(shape = RoundedCornerShape(50.dp),
            containerColor = colorResource(id = R.color.secondary),
            onClick = { interact() }) {

        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(76.dp, 56.dp),
        contentAlignment = BottomStart
    ) {
        ControlPad(moveUp, moveDown, moveLeft, moveRight)
    }
}

@Composable
private fun Character(charaState: CharaState) {

    val charaOffset: Offset by animateOffsetAsState(
        getOffset(
            nudge = charaState.nudge,
            jump = charaState.jump,
            direction = charaState.direction
        )
    )

    val charaSkin = when (charaState.direction) {
        Direction.UP -> R.drawable.chara_back
        Direction.DOWN -> R.drawable.chara_front
        Direction.RIGHT -> R.drawable.chara_right
        Direction.LEFT -> R.drawable.chara_left
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Center
    ) {
        Image(
            painter = painterResource(id = charaSkin),
            contentDescription = stringResource(id = R.string.main_character),
            modifier = Modifier
                .width(62.dp)
                .height(73.dp)
                .offset(charaOffset.x.dp, charaOffset.y.dp)
        )
    }
}

@Composable
private fun Enemy(enemyState: EnemyState, backgroundPos: CoordinatesDp) {
    val enemyOffset: Offset by animateOffsetAsState(
        getOffset(
            nudge = enemyState.nudge,
            jump = enemyState.jump,
            direction = enemyState.direction
        )
    )

    val skin = when (enemyState.type) {
        EnemyEnum.SLIME -> when (enemyState.direction) {
            Direction.UP -> R.drawable.slime_back
            Direction.DOWN -> R.drawable.slime_front
            Direction.LEFT -> R.drawable.slime_left
            Direction.RIGHT -> R.drawable.slime_right
        }
        EnemyEnum.WOLF -> when (enemyState.direction) {
            Direction.UP -> R.drawable.wolf_back
            Direction.DOWN -> R.drawable.wolf_front
            Direction.LEFT -> R.drawable.wolf_left
            Direction.RIGHT -> R.drawable.wolf_right
        }
        EnemyEnum.OGRE -> when (enemyState.direction) {
            Direction.UP -> R.drawable.ogre_back
            Direction.DOWN -> R.drawable.ogre_back
            Direction.LEFT -> R.drawable.ogre_left
            Direction.RIGHT -> R.drawable.ogre_right
        }
    }
    val position = getPositionFromCoordinates(backgroundPos, enemyState.position, enemyState.type == EnemyEnum.OGRE)

    Box(modifier = Modifier.fillMaxSize().offset(position.x, position.y)){
        Image(
            painter = painterResource(id = skin),
            contentDescription = stringResource(id = R.string.enemy),
            modifier = Modifier
                .width(62.dp)
                .height(73.dp)
                .offset(enemyOffset.x.dp, enemyOffset.y.dp)
        )
    }

}


@Preview
@Composable
fun ControlPad(
    moveUp: () -> Unit = {},
    moveDown: () -> Unit = {},
    moveLeft: () -> Unit = {},
    moveRight: () -> Unit = {},
) {
    Column {
        MoveButton(modifier = Modifier.align(CenterHorizontally)) { moveUp() }
        Row(Modifier.width(48.dp * 3), horizontalArrangement = Arrangement.SpaceBetween) {
            MoveButton(modifier = Modifier.rotate(-90f)) { moveLeft() }
            MoveButton(modifier = Modifier.rotate(90f)) { moveRight() }
        }
        MoveButton(
            modifier = Modifier
                .rotate(180f)
                .align(CenterHorizontally)
        ) { moveDown() }
    }
}

@Composable
fun MoveButton(modifier: Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .then(modifier)
            .clip(triangleShape)
            .size(48.dp),
        containerColor = colorResource(id = R.color.secondary),
    ) {
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun GamePreview() {
    GameScreen(charaState = CharaState(direction = Direction.DOWN, nudge = false, jump = false,
        position = Coordinates(0,0)
    ),
        enemiesState = listOf(),
        {}, {}, {}, {}, {}
    )
}

fun getOffset(jump: Boolean, nudge: Boolean, direction: Direction): Offset {

    if (!jump && !nudge){
        return Offset(0f, 0f)
    }
    if (jump){
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

fun getPositionFromCoordinates(
    backgroundPos: CoordinatesDp,
    coords: Coordinates,
    isOgre: Boolean = false
): CoordinatesDp {
    val moveLength = Settings.moveLength
    var xPos = backgroundPos.x + (coords.x * moveLength).dp + Settings.margin.dp
    var yPos = backgroundPos.y + (coords.y * moveLength).dp + Settings.margin.dp
    if (isOgre) {
        xPos -= 70.dp
        yPos -= 70.dp
    }
    return CoordinatesDp(xPos, yPos)
}
