package com.gabrielfreire.runandlift.feature.trainer.menu

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.navigation.SavedConfirmation
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerRoutes
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.trainerTabBar

/**
 * Liga o menu do treinador ao seu ViewModel.
 *
 * [onSignedOut] só é chamado depois de a sessão terminar — navegar antes mostraria a tela de
 * entrada com a sessão ainda ativa.
 *
 * @param onOpen recebe a rota a abrir. Uma função para os dois destinos, e não uma por item: quem
 *   sabe navegar é o grafo, e o menu só diz para onde.
 */
@Composable
internal fun TrainerMenuDestination(
    navController: NavHostController,
    dependencies: TrainerDependencies,
    onSignedOut: () -> Unit,
    onSwitchRole: (() -> Unit)?,
    onOpen: (String) -> Unit,
    viewModel: TrainerMenuViewModel = viewModel(
        factory = viewModelFactory {
            initializer { TrainerMenuViewModel(dependencies.authRepository) }
        },
    ),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Esta é a tela para a qual se volta depois de salvar em "Meus dados" ou no perfil profissional,
    // e é aqui que o recibo daquela gravação aparece. Ver `SavedResult`.
    SavedConfirmation(
        navController = navController,
        route = TrainerRoutes.MENU,
        snackbarHostState = snackbarHostState,
        message = stringResource(R.string.trainer_saved),
    )

    TrainerMenuScreen(
        tabs = trainerTabBar(navController = navController, current = TrainerTab.MENU),
        snackbarHostState = snackbarHostState,
        actions = TrainerMenuActions(
            onOpenAccount = { onOpen(TrainerRoutes.ACCOUNT) },
            onOpenProfile = { onOpen(TrainerRoutes.PROFILE) },
            onSignOut = { viewModel.signOut(onSignedOut) },
        ),
        onSwitchRole = onSwitchRole,
    )
}
