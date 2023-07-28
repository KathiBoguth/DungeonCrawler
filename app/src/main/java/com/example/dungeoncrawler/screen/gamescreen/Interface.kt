package com.example.dungeoncrawler.screen.gamescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dungeoncrawler.R

@Composable
fun Controls(
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
        contentAlignment = Alignment.BottomEnd
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
        contentAlignment = Alignment.BottomStart
    ) {
        ControlPad(moveUp, moveDown, moveLeft, moveRight)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(76.dp, 56.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Stats(gold, health, levelCount)
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
            contentAlignment = Alignment.Center
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


@Preview
@Composable
fun ControlPad(
    moveUp: () -> Unit = {},
    moveDown: () -> Unit = {},
    moveLeft: () -> Unit = {},
    moveRight: () -> Unit = {},
) {
    Column {
        MoveButton(modifier = Modifier.align(Alignment.CenterHorizontally)) { moveUp() }
        Row(Modifier.width(48.dp * 3), horizontalArrangement = Arrangement.SpaceBetween) {
            MoveButton(modifier = Modifier.rotate(-90f)) { moveLeft() }
            MoveButton(modifier = Modifier.rotate(90f)) { moveRight() }
        }
        MoveButton(
            modifier = Modifier
                .rotate(180f)
                .align(Alignment.CenterHorizontally)
        ) { moveDown() }
    }
}