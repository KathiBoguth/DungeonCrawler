package com.example.dungeoncrawler.screen.gamescreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.CharaScreenState
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction

@Composable
fun CharacterScreen(charaScreenState: CharaScreenState) {

    val charaOffset: Offset by animateOffsetAsState(
        getOffset(
            nudge = charaScreenState.nudge,
            jump = charaScreenState.jump,
            direction = charaScreenState.direction
        )
    )

    val flashColor = if (charaScreenState.flashRed) {
        colorResource(id = R.color.red_semitransparent)
    } else {
        colorResource(id = R.color.transparent)
    }
    val animatedFlashColor: Color by animateColorAsState(
        targetValue = flashColor,
        animationSpec = tween(durationMillis = Settings.animDuration.toInt())
    )

    val charaSkin = {
        when (charaScreenState.direction) {
            Direction.UP -> R.drawable.chara_back
            Direction.DOWN -> R.drawable.chara_front
            Direction.RIGHT -> R.drawable.chara_right
            Direction.LEFT -> R.drawable.chara_left
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = charaSkin()),
            contentDescription = stringResource(id = R.string.main_character),
            modifier = Modifier
                .width(62.dp)
                .height(73.dp)
                .offset { IntOffset(charaOffset.x.dp.roundToPx(), charaOffset.y.dp.roundToPx()) },
            colorFilter = ColorFilter.tint(animatedFlashColor, BlendMode.SrcAtop)
        )
        if (charaScreenState.fixated) {
            Image(
                painter = painterResource(id = R.drawable.tendril),
                contentDescription = stringResource(id = R.string.tendril),
                modifier = Modifier
                    .width(62.dp)
                    .height(73.dp)
                    .offset {
                        IntOffset(
                            charaOffset.x.dp.roundToPx(),
                            charaOffset.y.dp.roundToPx()
                        )
                    },
            )
        }
    }
}

@Preview
@Composable
fun CharaPreview() {
    val charaScreenState = CharaScreenState(
        nudge = false,
        cuirassId = "",
        jump = false,
        direction = Direction.DOWN,
        fixated = false,
        flashRed = false,
        weaponId = "",
        gold = 0,
        health = 100,
        position = Coordinates(5, 5),
        bombAmount = 1
    )

    CharacterScreen(charaScreenState = charaScreenState)
}