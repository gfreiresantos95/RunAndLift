package com.gabrielfreire.runandlift.feature.trainer.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.trainerTabBar

/** Liga o início do treinador ao seu ViewModel e às abas. */
@Composable
internal fun TrainerHomeDestination(
    navController: NavHostController,
    dependencies: TrainerDependencies,
    viewModel: TrainerHomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                TrainerHomeViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TrainerHomeScreen(
        state = state,
        tabs = trainerTabBar(navController = navController, current = TrainerTab.HOME),
    )
}
