package com.example.dungeoncrawler.ground

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.EnemyState
import com.example.dungeoncrawler.data.LevelObjectState
import com.example.dungeoncrawler.entity.CoordinatesDp
import com.example.dungeoncrawler.entity.GroundType
import kotlin.random.Random


@Composable
fun BackgroundComposable(
    backgroundPosition: CoordinatesDp,
    enemiesState: List<EnemyState>,
    objectsState: List<LevelObjectState>,
    levelCount: Int
) {
    val backgroundOffset: Offset by animateOffsetAsState(
        targetValue = Offset(backgroundPosition.x.value, backgroundPosition.y.value),
        label = "backgroundOffsetAnimation"
    )

    val backgroundLayout: List<List<GroundType>> by remember(key1 = levelCount) {
        mutableStateOf(computeBackgroundLayout())
    }

    val tileSize = Settings.moveLength

    Box(
        modifier = Modifier
            .wrapContentSize(unbounded = true)
            .offset(backgroundOffset.x.dp, backgroundOffset.y.dp)
    ) {

        Row(
            modifier = Modifier
                .wrapContentSize(unbounded = true)
        ) {
            for (row in backgroundLayout) {
                Column(modifier = Modifier.wrapContentSize(unbounded = true)) {
                    for (item in row) {
                        Image(
                            painter = painterResource(id = item.getDrawableId()),
                            contentDescription = stringResource(id = R.string.ground),
                            modifier = Modifier
                                .wrapContentSize(unbounded = true)
                                .width(tileSize.dp)
                                .height(tileSize.dp)
                        )
                    }
                }
            }
        }
        enemiesState.filter { it.visible }.forEach { enemy ->
            EnemyScreen(enemy, backgroundPosition)
        }
        objectsState.forEach {levelObject ->
            LevelObjectScreen(objectState = levelObject, backgroundPos = backgroundPosition)
        }
    }
}

fun randomGroundType(random: Random) : GroundType {

    return when( random.nextInt(4) ) {
        0 -> GroundType.GROUND1
        1 -> GroundType.GROUND2
        2 -> GroundType.PEBBLES
        3 -> GroundType.WATER
        else -> GroundType.GROUND1
    }
}

fun computeBackgroundLayout(): List<List<GroundType>> {
    val random = Random(System.currentTimeMillis())
    val layout = MutableList(Settings.fieldSize - 2) {
        MutableList(2) { GroundType.STONE }
    }
    layout.forEach {
        val newFields = MutableList(Settings.fieldSize - 2) {randomGroundType(random)}
        it.addAll(1, newFields)
    }
    layout.add(0, MutableList(Settings.fieldSize) { GroundType.STONE })
    layout.add( MutableList(Settings.fieldSize) { GroundType.STONE })
    return layout
}

fun GroundType.getDrawableId()  = when (this) {
        GroundType.GROUND1 -> R.drawable.ground
        GroundType.GROUND2 -> R.drawable.ground2
        GroundType.PEBBLES -> R.drawable.pebbles
        GroundType.WATER -> R.drawable.water
        GroundType.STONE -> R.drawable.stone_wall
}