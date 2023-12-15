package com.example.dungeoncrawler.screen.gamescreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.Level.Companion.BOW_WOODEN
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_IRON
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_RAG
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_IRON
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_WOODEN

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Controls(
    interact: () -> Unit,
    placeBomb: () -> Unit,
    move: (Direction) -> Unit,
    pause: () -> Unit,
    gold: Int,
    health: Int,
    levelCount: Int,
    bombAmount: Int,
    weaponId: String,
    cuirassId: String
) {
    val colorSecondary = colorResource(id = R.color.secondary)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(76.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .size(76.dp)
                .drawBehind { drawRect(colorSecondary) }
                .combinedClickable(
                    onClick = { interact() },
                    onLongClick = { placeBomb() })
        ) {
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        ControlPad(move)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.TopStart
    ) {
        val colorBackground = colorResource(id = R.color.blue_transparent)
        Row(Modifier.drawBehind { drawRect(colorBackground) }) {
            Stats(gold, health, levelCount)
            Inventory(weaponId, cuirassId, bombAmount)
        }

    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        PauseButton(pause)
    }
}

@Composable
fun MoveButton(modifier: Modifier, onClick: () -> Unit) {
    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = colorResource(id = R.color.red_transparent),
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
        ) {
            val colorSecondary = colorResource(id = R.color.secondary)
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(triangleShape)
                    .drawBehind { drawRect(colorSecondary) }
            )
        }
    }

}

@Composable
fun Stats(gold: Int, health: Int, levelCount: Int) {
    Column {
        Box(contentAlignment = Alignment.Center)
        {
            StatsText(text = stringResource(id = R.string.gold, gold))
        }
        Box {
            StatsText(stringResource(id = R.string.health, health))
        }
        Box {
            StatsText(text = stringResource(id = R.string.level, levelCount))
        }
    }

}

@Composable
fun StatsText(text: String) {
    Text(
        modifier = Modifier
            .padding(4.dp)
            .width(65.dp)
            .height(22.dp),
        text = text,
        fontFamily = FontFamily(Font(R.font.carrois_gothic_sc)),
        color = Color.White
    )
}

@Composable
fun Inventory(weaponId: String, cuirassId: String, bombAmount: Int) {
    val weaponResource = {
        when (weaponId) {
            SWORD_WOODEN -> R.drawable.sword_wooden
            SWORD_IRON -> R.drawable.sword_iron
            SWORD_DIAMOND -> R.drawable.sword_diamond
            BOW_WOODEN -> R.drawable.bow
            else -> -1
        }
    }
    val cuirassResource = {
        when (cuirassId) {
            CUIRASS_RAG -> R.drawable.cuirass_rag
            CUIRASS_IRON -> R.drawable.cuirass_iron
            CUIRASS_DIAMOND -> R.drawable.cuirass_diamond
            else -> -1
        }
    }
    Column {
        val weapon = weaponResource()
        if (weapon != -1) {
            Image(
                painter = painterResource(id = weapon),
                contentDescription = stringResource(id = R.string.wooden_sword),
                modifier = Modifier
                    .width(26.dp)
                    .height(27.dp)
            )
        }
        val cuirass = cuirassResource()
        if (cuirass != -1) {
            Image(
                painter = painterResource(id = cuirass),
                contentDescription = stringResource(id = R.string.iron_cuirass),
                modifier = Modifier
                    .width(26.dp)
                    .height(27.dp)
            )
        }
        if (bombAmount > 0) {
            BombInventorySymbol(bombAmount)
        }
    }
}

@Composable
fun BombInventorySymbol(amount: Int) {
    val bombResource = R.drawable.bomb_unlit

    Box {
        Image(
            painter = painterResource(id = bombResource),
            contentDescription = stringResource(id = R.string.bomb),
            modifier = Modifier
                .width(26.dp)
                .height(27.dp)
        )
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(end = 3.dp)
                .align(Alignment.BottomEnd)
        ) {
            Text(text = amount.toString(), color = colorResource(id = R.color.primary))
        }
    }

}


@Preview
@Composable
fun ControlPad(
    move: (Direction) -> Unit = {}
) {
    Column {
        MoveButton(modifier = Modifier.align(Alignment.CenterHorizontally)) { move(Direction.UP) }
        Row(Modifier.width(48.dp * 3 + 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            MoveButton(modifier = Modifier.graphicsLayer {
                rotationZ = -90f
            }) { move(Direction.LEFT) }
            MoveButton(modifier = Modifier.graphicsLayer {
                rotationZ = 90f
            }) { move(Direction.RIGHT) }
        }
        MoveButton(
            modifier = Modifier
                .graphicsLayer { rotationZ = 180f }
                .align(Alignment.CenterHorizontally)
        ) { move(Direction.DOWN) }
    }
}

@Composable
fun PauseButton(pause: () -> Unit) {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        FloatingActionButton(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(50.dp),
            containerColor = colorResource(id = R.color.blue_transparent),
            onClick = { pause() }) {

        }
        Image(
            painter = painterResource(id = R.drawable.pause_symbol),
            contentDescription = "Pause",
            modifier = Modifier.fillMaxSize(0.75f)
        )
    }

}

@Preview
@Composable
fun PausePreview() {
    PauseButton {

    }
}
