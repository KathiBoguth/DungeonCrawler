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
import androidx.compose.ui.unit.dp
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.data.LevelObjectState
import com.example.dungeoncrawler.entity.CoordinatesDp
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.Level.Companion.BOW_WOODEN
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_IRON
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_RAG
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_WOODEN
import com.example.dungeoncrawler.entity.LevelObjectType

@Composable
fun LevelObjectScreen(objectState: LevelObjectState, backgroundPos: CoordinatesDp) {
    val position by remember(key1 = objectState.position, key2 = backgroundPos) {
        val position = getPositionFromCoordinates(objectState.position)
        return@remember mutableStateOf(position)
    }

    val positionAsOffset: Offset by
    animateOffsetAsState(Offset(position.x.value, position.y.value), label = "level object offset")

    val skin = when (objectState.type) {
        LevelObjectType.MAIN_CHARA -> -1
        LevelObjectType.WALL -> -1
        LevelObjectType.TREASURE -> R.drawable.treasure
        LevelObjectType.LADDER -> R.drawable.ladder
        LevelObjectType.ENEMY -> -1
        LevelObjectType.COIN -> R.drawable.coin
        LevelObjectType.POTION -> R.drawable.potion
        LevelObjectType.WEAPON -> {
            when (objectState.id) {
                BOW_WOODEN -> R.drawable.bow
                SWORD_WOODEN -> R.drawable.sword_wooden
                SWORD_DIAMOND -> R.drawable.sword_diamond
                else -> -1
            }
        }

        LevelObjectType.ARROW -> {
            when (objectState.direction) {
                Direction.DOWN -> R.drawable.arrow_down
                Direction.LEFT -> R.drawable.arrow_left
                Direction.RIGHT -> R.drawable.arrow_right
                Direction.UP -> R.drawable.arrow_up
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

    if (skin != -1){
        Box(modifier = Modifier
            .fillMaxSize()
            .offset(positionAsOffset.x.dp, positionAsOffset.y.dp)
            .wrapContentSize(unbounded = true)) {
            Image(
                painter = painterResource(id = skin),
                contentDescription = stringResource(id = R.string.levelObject),
                modifier = Modifier
                    .width(62.dp)
                    .height(73.dp)
            )
        }
    }
}