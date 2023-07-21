package com.example.dungeoncrawler

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
        gameViewModel::moveRight,
        0 // TODO
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

    BackgroundComposable(backgroundPosition, enemiesState)

    Box(modifier = Modifier.fillMaxSize()) {
        Character(charaState)

        Controls(interact, moveUp, moveDown, moveLeft, moveRight, charaState.gold, charaState.health, levelCount)

    }

}

@Composable
private fun Controls(
    interact: () -> Unit,
    moveUp: () -> Unit,
    moveDown: () -> Unit,
    moveLeft: () -> Unit,
    moveRight: () -> Unit,
    gold: Int,
    health: Int,
    levelCount: Int
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(76.dp, 56.dp),
        contentAlignment = TopStart
    ) {
        Stats(gold, health, levelCount)
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

    val flashColor = if (charaState.flashRed) {
        colorResource(id = R.color.red_semitransparent)
    } else {
        colorResource(id = R.color.transparent)
    }
    val animatedFlashColor: Color by animateColorAsState(targetValue = flashColor, animationSpec = tween(durationMillis = Settings.animDuration.toInt()) )

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
                .offset(charaOffset.x.dp, charaOffset.y.dp),
            colorFilter = ColorFilter.tint(animatedFlashColor, BlendMode.SrcAtop)
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

@Composable
fun Stats(gold: Int, health: Int, levelCount: Int) {
    Column{
        Box(modifier = Modifier.background(colorResource(id = R.color.blue_transparent) ),
            contentAlignment = Center
        ){
            StatsText(text = stringResource(id = R.string.gold, gold))
        }
        Box(modifier = Modifier.background(colorResource(id = R.color.blue_transparent))
        ){
            StatsText(stringResource(id = R.string.health, health))

        }
        Box(modifier = Modifier.background(colorResource(id = R.color.blue_transparent))
        ) {
            StatsText(text = stringResource(id = R.string.level, levelCount))
        }
    }

}

@Composable
fun StatsText(text: String) {
    Text(modifier = Modifier
        .padding(4.dp)
        .width(65.dp)
        .height(22.dp),
        text = text,
        fontFamily = FontFamily(Font(R.font.carrois_gothic_sc)),
        color = Color.White)
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun GamePreview() {
    GameScreen(charaState = CharaState(direction = Direction.DOWN, nudge = false, jump = false,
        position = Coordinates(0,0), flashRed = false, gold = 0, health = 0
    ),
        enemiesState = listOf(EnemyState("", false, false, Direction.DOWN, Coordinates(-2,-5), EnemyEnum.SLIME, flashRed = false)),
        {}, {}, {}, {}, {}, levelCount = 0
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
