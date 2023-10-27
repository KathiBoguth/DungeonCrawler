package com.example.dungeoncrawler.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.screen.mainMenu.MenuText
import com.example.dungeoncrawler.screen.mainMenu.MenuTitle
import com.example.dungeoncrawler.screen.mainMenu.NavigationButton
import com.example.dungeoncrawler.viewmodel.MenuViewModel

@Composable
fun GameOverScreen(
    onRestartClicked: () -> Unit,
    onUpgradeStatsClicked: () -> Unit,
    pauseMusicPlayer: () -> Unit,
    @DrawableRes killedBy: Int
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        MenuTitle(stringResource(R.string.game_over))
        Row(
            Modifier
                .fillMaxWidth()
                .offset((-10).dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            MenuText(text = stringResource(id = R.string.killed_by_message))
            Box(Modifier.offset((-40).dp)) {
                Image(
                    painter = painterResource(id = killedBy),
                    contentDescription = stringResource(id = R.string.enemy),
                    modifier = Modifier
                        .width(62.dp)
                        .height(73.dp)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavigationButton(
                stringResource(id = R.string.restart),
                onRestartClicked, pauseMusic = true, pauseMusicPlayer = pauseMusicPlayer
            )
            NavigationButton(
                text = stringResource(id = R.string.upgrade_stats),
                onUpgradeStatsClicked,
                primary = false
            )
        }
    }
}

@Composable
fun GameOverScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onRestartClicked: () -> Unit,
    onUpgradeStatsClicked: () -> Unit,
    menuViewModel: MenuViewModel = viewModel()
) {

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        menuViewModel.startMediaPlayer()
    }

    DisposableEffect(lifecycleOwner) {
        menuViewModel.setupMediaPlayer(context)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                menuViewModel.startMediaPlayer()
            } else if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                menuViewModel.pauseMediaPlayer()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val killedBySkin = when (menuViewModel.killedBy.enemyType) {
        EnemyEnum.SLIME -> R.drawable.slime_front
        EnemyEnum.WOLF -> R.drawable.wolf_front
        EnemyEnum.OGRE -> R.drawable.ogre_front
        EnemyEnum.PLANT -> R.drawable.plant_front
    }

    GameOverScreen(
        onRestartClicked = onRestartClicked,
        onUpgradeStatsClicked = onUpgradeStatsClicked,
        pauseMusicPlayer = menuViewModel::pauseMediaPlayer,
        killedBy = killedBySkin
    )
}

@Preview
@Composable
fun GameOverPreview() {
    GameOverScreen(
        onRestartClicked = {},
        onUpgradeStatsClicked = {},
        pauseMusicPlayer = {},
        killedBy = R.drawable.wolf_front
    )
}
