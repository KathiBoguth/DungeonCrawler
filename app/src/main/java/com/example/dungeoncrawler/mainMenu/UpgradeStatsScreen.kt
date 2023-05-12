package com.example.dungeoncrawler.mainMenu

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.viewmodel.MenuViewModel
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.StatsUpgradeUiState

@Composable
fun UpgradeStatsScreen(
    onNavigate: (Int) -> Unit,
    menuViewModel: MenuViewModel = viewModel()
) {
    val state by menuViewModel.uiState.collectAsState()

    fun saveAndReturnToMain(context: Context) {
        menuViewModel.returnToMain(onNavigate, R.id.action_statsUpgradeFragment_to_mainFragment, context)
    }
    UpgradeStatsScreen(
        statsUpgradeUiState = state,
        saveAndReturnToMain = ::saveAndReturnToMain,
        onHealthPlusButtonClicked = menuViewModel::onHealthPlusButtonClicked,
        onHealthMinusButtonClicked = menuViewModel::onHealthPlusButtonClicked,
        onAttackPlusButtonClicked = menuViewModel::onHealthPlusButtonClicked,
        onAttackMinusButtonClicked = menuViewModel::onHealthPlusButtonClicked,
        onDefensePlusButtonClicked = menuViewModel::onHealthPlusButtonClicked,
        onDefenseMinusButtonClicked = menuViewModel::onHealthPlusButtonClicked,
        reset = menuViewModel::reset
    )

}

@Composable
fun  UpgradeStatsScreen(
    statsUpgradeUiState: StatsUpgradeUiState,
    saveAndReturnToMain: (Context) -> Unit,
    onHealthPlusButtonClicked: () -> Unit,
    onHealthMinusButtonClicked: () -> Unit,
    onAttackPlusButtonClicked: () -> Unit,
    onAttackMinusButtonClicked: () -> Unit,
    onDefensePlusButtonClicked: () -> Unit,
    onDefenseMinusButtonClicked: () -> Unit,
    reset: () -> Unit
) {
    //val menuViewModel: MenuViewModel = viewModel()

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
        MenuTitle(text = stringResource(R.string.upgrade_stats))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(
                Modifier
                    .height(180.dp)
                    .fillMaxWidth(0.5F),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start)
            {
                UpgradeStatRow(
                    statText = stringResource(R.string.health, statsUpgradeUiState.initialData.health),
                    upgradeValueText = stringResource(R.string.upgrade, statsUpgradeUiState.healthUpgrade),
                    onClickPlus = onHealthPlusButtonClicked,
                    onClickMinus = onHealthMinusButtonClicked,
                    plusButtonEnabled = statsUpgradeUiState.healthUpgradeAffordable,
                    minusButtonEnabled = statsUpgradeUiState.healthUpgradeButtonEnabled
                )
                UpgradeStatRow(
                    statText = stringResource(R.string.attack, statsUpgradeUiState.initialData.attack),
                    upgradeValueText = stringResource(R.string.upgrade, statsUpgradeUiState.attackUpgrade),
                    onClickPlus = onAttackPlusButtonClicked,
                    onClickMinus = onAttackMinusButtonClicked,
                    plusButtonEnabled = statsUpgradeUiState.attackUpgradeAffordable,
                    minusButtonEnabled = statsUpgradeUiState.attackUpgradeButtonEnabled
                )
                UpgradeStatRow(
                    statText = stringResource(R.string.defense, statsUpgradeUiState.initialData.defense),
                    upgradeValueText = stringResource(R.string.upgrade, statsUpgradeUiState.defenseUpgrade),
                    onClickPlus = onDefensePlusButtonClicked,
                    onClickMinus = onDefenseMinusButtonClicked,
                    plusButtonEnabled = statsUpgradeUiState.defenseUpgradeAffordable,
                    minusButtonEnabled = statsUpgradeUiState.defenseUpgradeButtonEnabled
                )
            }
            Row(Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text(text = stringResource(R.string.gold, statsUpgradeUiState.initialData.gold), color = Color.White, fontSize = 20.sp)
                Text(text = stringResource(R.string.minus, statsUpgradeUiState.goldCost), color = colorResource(id = R.color.red), fontSize = 20.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            val context = LocalContext.current
            MenuButton(stringResource(id = R.string.reset)) {reset()}
            MenuButton(text = stringResource(id = R.string.returnToMain)) { saveAndReturnToMain(context) }
        }
    }
}

@Composable
fun UpgradeStatRow(
    statText: String,
    upgradeValueText: String,
    onClickPlus: () -> Unit,
    plusButtonEnabled: Boolean,
    minusButtonEnabled: Boolean,
    onClickMinus: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = statText, color = Color.White, modifier = Modifier.requiredWidth(100.dp), fontSize = 20.sp)
        Text(text = upgradeValueText, color = colorResource(id = R.color.secondary), fontSize = 20.sp, modifier = Modifier.padding(
            PaddingValues(horizontal = 30.dp)
        ))
        Row(Modifier.width(120.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            RoundButton(content = { Icon(Icons.Default.Add, contentDescription = "plus") }, plusButtonEnabled) {
                onClickPlus()
            }
            RoundButton(content = { Icon(painterResource(id = R.drawable.baseline_remove_24), contentDescription = "minus") }, minusButtonEnabled) {
                onClickMinus()
            }
        }
    }
}

@Composable
fun RoundButton(content: @Composable () -> Unit, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.primary),
            disabledContainerColor = colorResource(id = R.color.disabled_button),
            disabledContentColor = colorResource(id = R.color.white_semitransparant)
        ),
        shape = CircleShape,
        modifier = Modifier
            .width(48.dp)
            .height(48.dp),
        contentPadding = PaddingValues(0.dp),
        enabled = enabled
    ) {
        content()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun UpgradeStatsPreview() {
    val stats = StatsUpgradeUiState()
    UpgradeStatsScreen(
        statsUpgradeUiState = stats,
        saveAndReturnToMain = { },
        onHealthPlusButtonClicked = {},
        onHealthMinusButtonClicked = {},
        onAttackPlusButtonClicked = {},
        onAttackMinusButtonClicked = {},
        onDefensePlusButtonClicked = {},
        onDefenseMinusButtonClicked = {},
        reset = {}
    )
}

@Preview
@Composable
fun UpgradeStatRowPreview() {
    UpgradeStatRow(
        statText = stringResource(R.string.health, 0),
        upgradeValueText = stringResource(R.string.upgrade, 0),
        onClickPlus = {},
        onClickMinus = {},
        plusButtonEnabled = true,
        minusButtonEnabled = false
    )
}