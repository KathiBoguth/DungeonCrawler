package com.example.dungeoncrawler.mainMenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.MenuViewModel

@Composable
fun MainMenuScreen(onNavigate: (Int) -> Unit) {
    val menuViewModel: MenuViewModel = viewModel()

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.main_menu), color = Color.White, modifier = Modifier.padding(80.dp), fontSize = 32.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavigationButton(stringResource(id = R.string.start_game),
                onNavigate, R.id.action_mainFragment_to_gameView)
            NavigationButton(text = stringResource(id = R.string.upgrade_stats), onNavigate, R.id.action_mainFragment_to_statsUpgradeFragment)
        }

    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.primary)
        ),
        shape = RoundedCornerShape(5.dp),
        modifier = Modifier
            .width(160.dp)
            .wrapContentHeight()
        ) {
        Text(text = text)
    }
}

@Composable
fun NavigationButton(text: String, onNavigate: (Int) -> Unit, destination: Int){
    fun navigate() {
        //navController.navigate(destination)
        onNavigate(destination)
    }

    MenuButton(text = text, onClick = ::navigate)
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun MainMenu() {
    MainMenuScreen {}
}