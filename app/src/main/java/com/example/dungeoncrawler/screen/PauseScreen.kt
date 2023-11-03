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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.screen.mainMenu.HighscoreDisplay
import com.example.dungeoncrawler.screen.mainMenu.MenuTitle
import com.example.dungeoncrawler.screen.mainMenu.NavigationButton
import com.example.dungeoncrawler.viewmodel.MenuViewModel

@Composable
fun PauseScreen(
    onReturnClicked: () -> Unit,
    onGiveUpClicked: () -> Unit,
    pauseMusicPlayer: () -> Unit,
    highscore: Int
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        HighscoreDisplay(highscore)
        MenuTitle(stringResource(R.string.paused_title))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavigationButton(
                stringResource(id = R.string.returnToGame),
                onReturnClicked, pauseMusic = true, pauseMusicPlayer = pauseMusicPlayer
            )
            NavigationButton(
                text = stringResource(id = R.string.giveUp),
                onGiveUpClicked,
                primary = false
            )
        }
    }
}

@Composable
fun PauseScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onReturnClicked: () -> Unit,
    onGiveUpClicked: () -> Unit,
    highscore: Int,
    menuViewModel: MenuViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        menuViewModel.setupMediaPlayer(context)
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

    PauseScreen(
        onReturnClicked = onReturnClicked,
        onGiveUpClicked = onGiveUpClicked,
        pauseMusicPlayer = menuViewModel::pauseMediaPlayer,
        highscore = highscore
    )
}