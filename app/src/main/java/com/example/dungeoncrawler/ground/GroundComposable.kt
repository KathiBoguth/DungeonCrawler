package com.example.dungeoncrawler.ground

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.entity.CoordinatesDp
import com.example.dungeoncrawler.entity.GroundType
import kotlin.random.Random

val backgroundLayout = computeBackgroundLayout()
@Composable
fun BackgroundComposable(backgroundPosition: CoordinatesDp) {

    Row(modifier = Modifier
        .offset(backgroundPosition.x, backgroundPosition.y)
        .wrapContentSize(unbounded = true)) {
        for (row in backgroundLayout) {
            Column(modifier = Modifier.wrapContentSize(unbounded = true)) {
                for (item in row) {
                    Image(
                        painter = painterResource(id = item.getDrawableId()),
                        contentDescription = stringResource(id = R.string.ground),
                        modifier = Modifier
                            .wrapContentSize(unbounded = true)
                            .width(80.dp)
                            .height(80.dp)
                    )
                }
            }
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

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun BackgroundPreview() {
    BackgroundComposable(CoordinatesDp(405.dp, 440.dp))
}