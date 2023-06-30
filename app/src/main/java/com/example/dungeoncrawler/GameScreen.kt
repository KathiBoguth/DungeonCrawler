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
    LaunchedEffect(Unit) {
        gameViewModel.reset()
    }

    GameScreen(
        charaState,
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
    interact: () -> Unit,
    moveUp: () -> Unit,
    moveDown: () -> Unit,
    moveLeft: () -> Unit,
    moveRight: () -> Unit,
) {
    val charaOffset: Offset by animateOffsetAsState(
        getOffset(
            charaState.nudge,
            charaState.direction
        )
    )
    val charaSkin = when (charaState.direction) {
        Direction.UP -> R.drawable.chara_back
        Direction.DOWN -> R.drawable.chara_front
        Direction.RIGHT -> R.drawable.chara_right
        Direction.LEFT -> R.drawable.chara_left
    }

    //var backgroundOrigPosition by remember { mutableStateOf(CoordinatesDp(0.dp, 0.dp))  }
    val backgroundOrigPosition = CoordinatesDp(
        (LocalConfiguration.current.screenWidthDp/2).dp,
        (LocalConfiguration.current.screenHeightDp).dp
    )
    val backgroundPosition by remember(key1 = charaState.position, key2 = backgroundOrigPosition) {
        val moveLength = Settings.moveLength
        val xPosBackground = backgroundOrigPosition.x.minus((charaState.position.x*moveLength).dp)
        val yPosBackground = backgroundOrigPosition.y.minus((charaState.position.y*moveLength).dp)
        return@remember mutableStateOf(CoordinatesDp(xPosBackground, yPosBackground))
    }

    BackgroundComposable(backgroundPosition)
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
//                .onGloballyPositioned { coordinates ->
//                    val offset = coordinates.positionInParent()
//                    backgroundOrigPosition = CoordinatesDp(offset.x.dp, offset.y.dp)
//                },
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
    GameScreen(charaState = CharaState(direction = Direction.DOWN, nudge = false,
        position = Coordinates(0,0)
    ),
        {}, {}, {}, {}, {}
    )
}

@Composable
fun Coordinates.getPositionInPixels(
    isOgre: Boolean = false,
    backgroundPos: Coordinates
): Pair<Float, Float> {
    var xPos: Float
    var yPos: Float
    with(LocalDensity.current) {
        val moveLength = Settings.moveLength.dp.toPx()
        xPos = x * moveLength + backgroundPos.x + Settings.margin
        yPos = y * moveLength + backgroundPos.y + Settings.margin
        if (isOgre) {
            xPos -= 70f.dp.toPx()
            yPos -= 70f.dp.toPx()
        }
    }

    return Pair(xPos, yPos)
}

fun getOffset(nudge: Boolean, direction: Direction): Offset {

    return if (!nudge) {
        Offset(0f, 0f)
    } else {
        var deltaX = 0
        var deltaY = 0
        when (direction) {
            Direction.UP -> deltaY = -1
            Direction.DOWN -> deltaY = 1
            Direction.LEFT -> deltaX = -1
            Direction.RIGHT -> deltaX = 1
        }

        Offset(Settings.nudgeWidth * deltaX, Settings.nudgeWidth * deltaY)
    }
}
