package com.example.dungeoncrawler.screen.mainMenu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.viewmodel.MenuViewModel


@Composable
fun MainMenuScreen(
    onStartGameClicked: () -> Unit,
    onUpgradeStatsClicked: () -> Unit,
    pauseMusicPlayer: () -> Unit,
    highscore: Int
) {
    HighscoreDisplay(highscore)
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        MenuTitle(stringResource(R.string.main_menu))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavigationButton(
                stringResource(id = R.string.start_game),
                onStartGameClicked, pauseMusic = true, pauseMusicPlayer = pauseMusicPlayer
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
fun MainMenuScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onStartGameClicked: () -> Unit, onUpgradeStatsClicked: () -> Unit,
    menuViewModel: MenuViewModel = viewModel()
) {
    val highscore by menuViewModel.getHighscore().collectAsState(0)

    LaunchedEffect(Unit) {
        menuViewModel.mediaPlayerService.startMediaPlayerMenu()
    }

    DisposableEffect(lifecycleOwner) {
        menuViewModel.mediaPlayerService.pauseMediaPlayers()
        menuViewModel.mediaPlayerService.startMediaPlayerMenu()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                menuViewModel.mediaPlayerService.startMediaPlayerMenu()
            } else if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                menuViewModel.mediaPlayerService.pauseMediaPlayers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    MainMenuScreen(
        onStartGameClicked,
        onUpgradeStatsClicked,
        pauseMusicPlayer = menuViewModel.mediaPlayerService::pauseMediaPlayers,
        highscore = highscore
    )
}

@Composable
fun MenuButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.primary),
            disabledContainerColor = colorResource(id = R.color.disabled_button),
            disabledContentColor = colorResource(id = R.color.white_semitransparant)
        ),
        shape = RoundedCornerShape(5.dp),
        enabled = enabled,
        modifier = Modifier
            .width(240.dp)
            .wrapContentHeight()
    ) {
        Text(text = text)
    }
}

@Composable
fun MenuButtonSecondary(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val borderColor = if (enabled) R.color.primary else R.color.disabled_button
    OutlinedButton(
        onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.red_transparent),
            disabledContainerColor = colorResource(id = R.color.red_transparent),
            disabledContentColor = colorResource(id = R.color.white_semitransparant)
        ),
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(2.dp, colorResource(id = borderColor)),
        enabled = enabled,
        modifier = Modifier
            .width(240.dp)
            .wrapContentHeight()
    ) {
        Text(text = text)
    }
}

@Composable
fun NavigationButton(
    text: String,
    onNavigate: () -> Unit,
    pauseMusic: Boolean = false,
    pauseMusicPlayer: () -> Unit = {},
    primary: Boolean = true
) {
    fun navigate() {
        if (pauseMusic) {
            pauseMusicPlayer()
        }
        onNavigate()
    }
    if (primary) {
        MenuButton(text = text, onClick = ::navigate)
    } else {
        MenuButtonSecondary(text = text, onClick = ::navigate)
    }
}

@Composable
fun MenuTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        modifier = Modifier.padding(PaddingValues(horizontal = 80.dp)),
        fontSize = 32.sp,
        fontFamily = FontFamily(Font(R.font.carrois_gothic_sc))
    )
}

@Composable
fun HighscoreDisplay(highscore: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = stringResource(id = R.string.highscore),
            color = Color.White,
            fontSize = 26.sp,
            fontFamily = FontFamily(Font(R.font.carrois_gothic_sc))
        )
        Text(
            text = highscore.toString(),
            color = Color.White,
            modifier = Modifier.padding(PaddingValues(horizontal = 40.dp)),
            fontSize = 26.sp,
            fontFamily = FontFamily(Font(R.font.carrois_gothic_sc))
        )
    }

}

@Composable
fun MenuText(text: String) {
    Text(
        text = text,
        color = Color.White,
        modifier = Modifier.padding(PaddingValues(horizontal = 80.dp)),
        fontSize = 24.sp,
        fontFamily = FontFamily(Font(R.font.carrois_gothic_sc))
    )
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun MainMenuPreview() {
    MainMenuScreen(onStartGameClicked = {}, onUpgradeStatsClicked = {}, pauseMusicPlayer = {}, 0)
}

@Preview
@Composable
fun SecondaryButtonPreview() {
    MenuButtonSecondary(text = "Upgrade Stats", enabled = false) {

    }
}