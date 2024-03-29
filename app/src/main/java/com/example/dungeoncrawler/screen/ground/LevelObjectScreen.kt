package com.example.dungeoncrawler.screen.ground

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.LevelObjectState
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.CoordinatesDp
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.Level.Companion.BOW_WOODEN
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_IRON
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_RAG
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_IRON
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_WOODEN
import com.example.dungeoncrawler.entity.LevelObjectType

@Composable
fun LevelObjectScreen(objectState: LevelObjectState, backgroundPos: CoordinatesDp) {
    val position by remember(key1 = objectState.position, key2 = backgroundPos) {
        val position = getPositionFromCoordinates(
            objectState.position,
            isWall = objectState.type == LevelObjectType.WALL
        )
        return@remember mutableStateOf(position)
    }

    val positionAsOffset: Offset by animateOffsetAsState(
        Offset(position.x.value, position.y.value)
    )

    val skin = {
        when (objectState.type) {
            LevelObjectType.MAIN_CHARA -> -1
            LevelObjectType.WALL -> R.drawable.stone_wall
            LevelObjectType.TREASURE -> R.drawable.treasure
            LevelObjectType.TREASURE_DIAMOND -> R.drawable.treasure_diamond
            LevelObjectType.LADDER -> R.drawable.ladder
            LevelObjectType.ENEMY -> -1
            LevelObjectType.COIN -> R.drawable.coin
            LevelObjectType.DIAMOND -> R.drawable.diamond
            LevelObjectType.POTION -> R.drawable.potion
            LevelObjectType.BOMB -> {
                if (objectState.lit) {
                    R.drawable.bomb_lit
                } else {
                    R.drawable.bomb_unlit
                }
            }

            LevelObjectType.WEAPON -> {
                when (objectState.id) {
                    BOW_WOODEN -> R.drawable.bow
                    SWORD_WOODEN -> R.drawable.sword_wooden
                    SWORD_IRON -> R.drawable.sword_iron
                    SWORD_DIAMOND -> R.drawable.sword_diamond
                    else -> -1
                }
            }

            LevelObjectType.ARROW -> {
                if (objectState.id == "pebble_throwable") {
                    R.drawable.pebble_throwable
                } else {
                    when (objectState.direction) {
                        Direction.DOWN -> R.drawable.arrow_down
                        Direction.LEFT -> R.drawable.arrow_left
                        Direction.RIGHT -> R.drawable.arrow_right
                        Direction.UP -> R.drawable.arrow_up
                    }
                }
            }

            LevelObjectType.ARMOR -> {
                when (objectState.id) {
                    CUIRASS_RAG -> R.drawable.cuirass_rag
                    CUIRASS_IRON -> R.drawable.cuirass_iron
                    CUIRASS_DIAMOND -> R.drawable.cuirass_diamond
                    else -> -1
                }

            }
        }
    }

    if (skin() != -1) {
        Box(modifier = Modifier
            .fillMaxSize()
            .then(
                if (objectState.type == LevelObjectType.ARROW) {
                    Modifier.offset {
                        IntOffset(
                            positionAsOffset.x.dp.roundToPx(),
                            positionAsOffset.y.dp.roundToPx()
                        )
                    }
                } else {
                    // TODO: would need -20 to be in middle, but issue with stones
                    Modifier.offset {
                        IntOffset(
                            position.x.roundToPx(),
                            position.y.roundToPx()
                        )
                    }
                }
            )
            .wrapContentSize(unbounded = true)) {
            Image(
                painter = painterResource(id = skin()),
                contentDescription = stringResource(id = R.string.levelObject),
                modifier = Modifier
                    .then(
                        if (objectState.type == LevelObjectType.WALL) {
                            Modifier
                                .width(Settings.moveLength.dp)
                                .height(Settings.moveLength.dp)
                        } else {
                            Modifier
                                .width(62.dp)
                                .height(73.dp)
                        }
                    )

            )
        }
    }
}

@Composable
fun Explosion(explosionLevel: Int, positionObject: Coordinates, backgroundPos: CoordinatesDp) {
    val position by remember(key1 = positionObject, key2 = backgroundPos) {
        val position = getPositionFromCoordinates(
            positionObject,
            isWall = false
        )
        return@remember mutableStateOf(position)
    }

    val positionAsOffset: Offset by animateOffsetAsState(
        Offset(position.x.value, position.y.value)
    )

    val skin = when (explosionLevel) {
        1 -> R.drawable.explosion1
        2 -> R.drawable.explosion2
        3 -> R.drawable.explosion3
        4 -> R.drawable.explosion4
        5 -> R.drawable.explosion5
        6 -> R.drawable.explosion6
        7 -> R.drawable.explosion7
        8 -> R.drawable.explosion8
        else -> -1
    }
    if (skin != -1) {
        Box(modifier = Modifier
            .fillMaxSize()
            .then(
                if (explosionLevel > 2) {
                    Modifier.offset {
                        IntOffset(
                            (positionAsOffset.x - Settings.moveLength).dp.roundToPx(),
                            (positionAsOffset.y - Settings.moveLength).dp.roundToPx()
                        )
                    }
                } else {
                    Modifier.offset {
                        IntOffset(
                            positionAsOffset.x.dp.roundToPx(),
                            positionAsOffset.y.dp.roundToPx()
                        )
                    }
                }

            )
            .wrapContentSize(unbounded = true)) {
            Image(
                painter = painterResource(id = skin), contentDescription = "explosion",
                modifier = Modifier
                    .then(
                        if (explosionLevel > 2) {
                            Modifier
                                .width(Settings.moveLength.dp * 3)
                                .height(Settings.moveLength.dp * 3)
                        } else {
                            Modifier
                                .width(62.dp)
                                .height(73.dp)
                        }
                    )
            )
        }
    }
}