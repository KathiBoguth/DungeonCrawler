package com.example.dungeoncrawler.screen.ground

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.EnemyState
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.CoordinatesDp
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.screen.gamescreen.getOffset

@Composable
fun EnemyScreen(enemyState: EnemyState, backgroundPos: CoordinatesDp) {
    val enemyOffset: Offset by animateOffsetAsState(
        getOffset(
            nudge = enemyState.nudge,
            jump = enemyState.jump,
            direction = enemyState.direction
        )
    )
    val position by remember(key1 = enemyState.position, key2 = backgroundPos) {
        val position =
            getPositionFromCoordinates(enemyState.position, enemyState.type == EnemyEnum.OGRE)
        return@remember mutableStateOf(position)
    }

    val positionAsOffset: Offset by animateOffsetAsState(
        Offset(position.x.value, position.y.value)
    )

    val flashColor = if (enemyState.flashRed) {
        colorResource(id = R.color.red_semitransparent)
    } else {
        colorResource(id = R.color.transparent)
    }
    val animatedFlashColor: Color by animateColorAsState(
        targetValue = flashColor,
        animationSpec = tween(durationMillis = Settings.animDuration.toInt())
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

        EnemyEnum.PLANT -> when (enemyState.direction) {
            Direction.UP -> R.drawable.plant_back
            Direction.DOWN -> R.drawable.plant_front
            Direction.LEFT -> R.drawable.plant_left
            Direction.RIGHT -> R.drawable.plant_right
        }

        EnemyEnum.OGRE -> {
            if (enemyState.loadsAttack) {
                R.drawable.ogre_attack
            } else {
                when (enemyState.direction) {
                    Direction.UP -> R.drawable.ogre_back
                    Direction.DOWN -> R.drawable.ogre_front
                    Direction.LEFT -> R.drawable.ogre_left
                    Direction.RIGHT -> R.drawable.ogre_right
                }
            }
        }
    }
    val width = if (enemyState.type == EnemyEnum.OGRE) 140.dp else 62.dp
    val height = if (enemyState.type == EnemyEnum.OGRE) 140.dp else 73.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(positionAsOffset.x.dp, positionAsOffset.y.dp)
            .wrapContentSize(unbounded = true),
    ) {
        if (enemyState.healthPercentage < 1.0) {
            HealthBar(enemyState.healthPercentage, width)
        }

        Image(
            painter = painterResource(id = skin),
            contentDescription = stringResource(id = R.string.enemy),
            modifier = Modifier
                .width(width)
                .height(height)
                .offset(enemyOffset.x.dp, enemyOffset.y.dp),
            colorFilter = ColorFilter.tint(animatedFlashColor, BlendMode.SrcAtop)
        )
    }
}

@Composable
fun HealthBar(healthPercentage: Double, enemyWidth: Dp) {
    val healthBarFullWidth = 54
    val healthBarWidth = healthBarFullWidth * healthPercentage
    Box(modifier = Modifier.width(enemyWidth), contentAlignment = Center) {
        Box(
            modifier = Modifier
                .width(healthBarFullWidth.dp)
                .height(8.dp)
                .background(colorResource(id = R.color.grey))
        ) {
            Box(
                modifier = Modifier
                    .width(healthBarWidth.dp)
                    .height(8.dp)
                    .background(colorResource(id = R.color.red))
            )

        }


    }


}

@Preview
@Composable
fun EnemyPreview() {
    val enemyState = EnemyState(
        id = "",
        nudge = false,
        jump = false,
        direction = Direction.DOWN,
        position = Coordinates(0, 0),
        type = EnemyEnum.OGRE,
        flashRed = false,
        visible = true,
        loadsAttack = false,
        healthPercentage = 0.5
    )
    EnemyScreen(enemyState = enemyState, backgroundPos = CoordinatesDp(0.dp, 0.dp))
}

fun getPositionFromCoordinates(
    coords: Coordinates,
    isOgre: Boolean = false,
): CoordinatesDp {
    val moveLength = Settings.moveLength

    var xPos = (coords.x * moveLength).dp + Settings.margin.dp
    var yPos = (coords.y * moveLength).dp + Settings.margin.dp
    if (isOgre) {
        xPos -= 70.dp
        yPos -= 70.dp
    }
    return CoordinatesDp(xPos, yPos)
}
