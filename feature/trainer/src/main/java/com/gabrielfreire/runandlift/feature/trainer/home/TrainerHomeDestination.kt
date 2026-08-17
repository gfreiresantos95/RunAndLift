package com.gabrielfreire.runandlift.feature.trainer.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
 * Liga o início do treinador ao seu ViewModel e às abas.
 *
 * Relê o estado a cada volta para a tela (`LifecycleResumeEffect`), e não só na criação: quem
 * completa o perfil e volta precisa ver o aviso sumir. O ViewModel sobrevive à ida à edição — é o
 * mesmo destino na pilha —, então sem esta releitura o aviso continuaria lá, dizendo que falta o
 * que acabou de ser preenchido.
 */
@Composable
internal fun TrainerHomeDestination(
    navController: NavHostController,
    dependencies: TrainerDependencies,
    onOpenProfile: () -> Unit,
    viewModel: TrainerHomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                TrainerHomeViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                    trainerRepository = dependencies.trainerRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    // O aviso de perfil incompleto abre a edição a partir daqui, então esta aba também é destino de
    // volta — e o recibo da gravação precisa chegar nela, não só no menu.
    SavedConfirmation(
        navController = navController,
        route = TrainerRoutes.HOME,
        snackbarHostState = snackbarHostState,
        message = stringResource(R.string.trainer_saved),
    )

    TrainerHomeScreen(
        state = state,
        tabs = trainerTabBar(navController = navController, current = TrainerTab.HOME),
        onOpenProfile = onOpenProfile,
        snackbarHostState = snackbarHostState,
    )
}
