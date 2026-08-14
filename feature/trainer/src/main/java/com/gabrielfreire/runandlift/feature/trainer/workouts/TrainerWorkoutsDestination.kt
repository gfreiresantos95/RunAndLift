package com.gabrielfreire.runandlift.feature.trainer.workouts

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.trainerTabBar

/** Liga a aba de treinos do treinador às abas. Sem ViewModel enquanto não há programa que carregar. */
@Composable
internal fun TrainerWorkoutsDestination(navController: NavHostController) {
    TrainerWorkoutsScreen(
        tabs = trainerTabBar(navController = navController, current = TrainerTab.WORKOUTS),
    )
}
