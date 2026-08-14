package com.gabrielfreire.runandlift.feature.trainer.menu

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.trainerTabBar

/**
 * Liga o menu do treinador ao seu ViewModel.
 *
 * [onSignedOut] só é chamado depois de a sessão terminar — navegar antes mostraria a tela de
 * entrada com a sessão ainda ativa.
 */
@Composable
internal fun TrainerMenuDestination(
    navController: NavHostController,
    dependencies: TrainerDependencies,
    onSignedOut: () -> Unit,
    onSwitchRole: (() -> Unit)?,
    viewModel: TrainerMenuViewModel = viewModel(
        factory = viewModelFactory {
            initializer { TrainerMenuViewModel(dependencies.authRepository) }
        },
    ),
) {
    TrainerMenuScreen(
        tabs = trainerTabBar(navController = navController, current = TrainerTab.MENU),
        onSignOut = { viewModel.signOut(onSignedOut) },
        onSwitchRole = onSwitchRole,
    )
}
