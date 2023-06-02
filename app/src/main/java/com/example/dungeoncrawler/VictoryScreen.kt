import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.mainMenu.MenuTitle
import com.example.dungeoncrawler.mainMenu.NavigationButton
import com.example.dungeoncrawler.viewmodel.MenuViewModel

@Composable
fun VictoryScreen(onNavigate: (Int) -> Unit, pauseMusicPlayer: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
        MenuTitle(stringResource(R.string.victory))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavigationButton(
                stringResource(id = R.string.restart),
                onNavigate, R.id.action_victoryView_to_gameView, pauseMusic = true, pauseMusicPlayer = pauseMusicPlayer)
            NavigationButton(text = stringResource(id = R.string.upgrade_stats), onNavigate, R.id.action_victoryView_to_statsUpgradeFragment)
        }
    }
}

@Composable
fun VictoryScreen(
    onNavigate: (Int) -> Unit,
    menuViewModel: MenuViewModel = viewModel()
) {

    menuViewModel.setupMediaPlayer(LocalContext.current)
    menuViewModel.startMediaPlayer()

    VictoryScreen(
        onNavigate = onNavigate,
        pauseMusicPlayer = menuViewModel::pauseMediaPlayer
    )
}