import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.screen.mainMenu.MenuTitle
import com.example.dungeoncrawler.screen.mainMenu.NavigationButton
import com.example.dungeoncrawler.viewmodel.MenuViewModel

@Composable
fun VictoryScreen(
    onRestartClicked: () -> Unit,
    onUpgradeStatsClicked: () -> Unit,
    pauseMusicPlayer: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        MenuTitle(stringResource(R.string.victory))
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
fun VictoryScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onRestartClicked: () -> Unit,
    onUpgradeStatsClicked: () -> Unit,
    menuViewModel: MenuViewModel = viewModel()
) {

    LaunchedEffect(Unit) {
        menuViewModel.mediaPlayerService.pauseMediaPlayers()
        menuViewModel.mediaPlayerService.startMediaPlayerMenu()
    }

    DisposableEffect(lifecycleOwner) {
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

    VictoryScreen(
        onRestartClicked = onRestartClicked,
        onUpgradeStatsClicked = onUpgradeStatsClicked,
        pauseMusicPlayer = menuViewModel.mediaPlayerService::pauseMediaPlayers
    )
}