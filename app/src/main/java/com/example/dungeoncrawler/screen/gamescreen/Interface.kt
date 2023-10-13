package com.example.dungeoncrawler.screen.gamescreen

import androidx.compose.foundation.Image
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
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.entity.Level.Companion.BOW_WOODEN
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_IRON
import com.example.dungeoncrawler.entity.Level.Companion.CUIRASS_RAG
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_DIAMOND
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_IRON
import com.example.dungeoncrawler.entity.Level.Companion.SWORD_WOODEN

@Composable
fun Controls(
    interact: () -> Unit,
    moveUp: () -> Unit,
    moveDown: () -> Unit,
    moveLeft: () -> Unit,
    moveRight: () -> Unit,
    pause: () -> Unit,
    gold: Int,
    health: Int,
    levelCount: Int,
    weaponId: String,
    cuirassId: String
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
            .padding(20.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        ControlPad(moveUp, moveDown, moveLeft, moveRight)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Row(Modifier.background(colorResource(id = R.color.blue_transparent))) {
            Stats(gold, health, levelCount)
            Inventory(weaponId, cuirassId)
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
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(triangleShape)
                    .background(colorResource(id = R.color.secondary))
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
fun Inventory(weaponId: String, cuirassId: String) {
    val weaponResource = when (weaponId) {
        SWORD_WOODEN -> R.drawable.sword_wooden
        SWORD_IRON -> R.drawable.sword_iron
        SWORD_DIAMOND -> R.drawable.sword_diamond
        BOW_WOODEN -> R.drawable.bow
        else -> -1
    }
    val cuirassResource = when (cuirassId) {
        CUIRASS_RAG -> R.drawable.cuirass_rag
        CUIRASS_IRON -> R.drawable.cuirass_iron
        CUIRASS_DIAMOND -> R.drawable.cuirass_diamond
        else -> -1
    }

    Column {
        if (weaponResource != -1) {
            Image(
                painter = painterResource(id = weaponResource),
                contentDescription = stringResource(id = R.string.wooden_sword),
                modifier = Modifier
                    .width(26.dp)
                    .height(27.dp)
            )
        }
        if (cuirassResource != -1) {
            Image(
                painter = painterResource(id = cuirassResource),
                contentDescription = stringResource(id = R.string.iron_cuirass),
                modifier = Modifier
                    .width(26.dp)
                    .height(27.dp)
            )
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
        MoveButton(modifier = Modifier.align(Alignment.CenterHorizontally)) { moveUp() }
        Row(Modifier.width(48.dp * 3 + 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
