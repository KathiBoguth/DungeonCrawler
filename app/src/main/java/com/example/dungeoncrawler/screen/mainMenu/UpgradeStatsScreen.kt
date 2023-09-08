package com.example.dungeoncrawler.screen.mainMenu

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.data.StatsUpgradeUiState
import com.example.dungeoncrawler.viewmodel.MenuViewModel

@Composable
fun UpgradeStatsScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onNavigateBack: () -> Unit,
    menuViewModel: MenuViewModel = viewModel()
) {
    val state by menuViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        menuViewModel.loadStats()
    }

    DisposableEffect(lifecycleOwner) {
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

    fun saveAndReturnToMain() {
        menuViewModel.returnToMain(onNavigateBack)
    }
    UpgradeStatsScreen(
        statsUpgradeUiState = state,
        saveAndReturnToMain = ::saveAndReturnToMain,
        onHealthPlusButtonClicked = menuViewModel::onHealthPlusButtonClicked,
        onHealthMinusButtonClicked = menuViewModel::onHealthMinusButtonClicked,
        onAttackPlusButtonClicked = menuViewModel::onAttackPlusButtonClicked,
        onAttackMinusButtonClicked = menuViewModel::onAttackMinusButtonClicked,
        onDefensePlusButtonClicked = menuViewModel::onDefensePlusButtonClicked,
        onDefenseMinusButtonClicked = menuViewModel::onDefenseMinusButtonClicked,
        reset = menuViewModel::reset
    )

    DisposableEffect(Unit) {
        onDispose {
            menuViewModel.pauseMediaPlayer()
        }
    }

}

@Composable
fun  UpgradeStatsScreen(
    statsUpgradeUiState: StatsUpgradeUiState,
    saveAndReturnToMain: () -> Unit,
    onHealthPlusButtonClicked: () -> Unit,
    onHealthMinusButtonClicked: () -> Unit,
    onAttackPlusButtonClicked: () -> Unit,
    onAttackMinusButtonClicked: () -> Unit,
    onDefensePlusButtonClicked: () -> Unit,
    onDefenseMinusButtonClicked: () -> Unit,
    reset: () -> Unit
) {
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
                    upgradeValueText = stringResource(R.string.upgrade, statsUpgradeUiState.healthUpgrade* MenuViewModel.HEALTH_UPGRADE_MULTIPLIER),
                    onClickPlus = onHealthPlusButtonClicked,
                    onClickMinus = onHealthMinusButtonClicked,
                    plusButtonEnabled = statsUpgradeUiState.healthUpgradePlusButtonEnabled,
                    minusButtonEnabled = statsUpgradeUiState.healthUpgradeMinusButtonEnabled
                )
                UpgradeStatRow(
                    statText = stringResource(R.string.attack, statsUpgradeUiState.initialData.attack),
                    upgradeValueText = stringResource(R.string.upgrade, statsUpgradeUiState.attackUpgrade),
                    onClickPlus = onAttackPlusButtonClicked,
                    onClickMinus = onAttackMinusButtonClicked,
                    plusButtonEnabled = statsUpgradeUiState.attackUpgradePlusButtonEnabled,
                    minusButtonEnabled = statsUpgradeUiState.attackUpgradeMinusButtonEnabled
                )
                UpgradeStatRow(
                    statText = stringResource(R.string.defense, statsUpgradeUiState.initialData.defense),
                    upgradeValueText = stringResource(R.string.upgrade, statsUpgradeUiState.defenseUpgrade),
                    onClickPlus = onDefensePlusButtonClicked,
                    onClickMinus = onDefenseMinusButtonClicked,
                    plusButtonEnabled = statsUpgradeUiState.defenseUpgradePlusButtonEnabled,
                    minusButtonEnabled = statsUpgradeUiState.defenseUpgradeMinusButtonEnabled
                )
            }
            Row(Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text(text = stringResource(R.string.gold, statsUpgradeUiState.initialData.gold), color = Color.White, fontSize = 20.sp)
                Text(text = stringResource(R.string.minus, statsUpgradeUiState.goldCost), color = colorResource(id = R.color.red), fontSize = 20.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            val returnButtonText = if (statsUpgradeUiState.isAnyUpgradeSelected){
                stringResource(id = R.string.applyAndReturn)
            } else {
                stringResource(id = R.string.returnToMain)
            }
            MenuButton(stringResource(id = R.string.reset), enabled = statsUpgradeUiState.isAnyUpgradeSelected) {reset()}
            MenuButton(text = returnButtonText) { saveAndReturnToMain() }
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