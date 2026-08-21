package com.gabrielfreire.runandlift.feature.trainer.programs

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
 * Liga a aba de treinos ao seu ViewModel e às abas.
 *
 * Relê a cada volta para a aba, e aqui o motivo é o editor logo ao lado: sair dele depois de salvar
 * e encontrar a lista sem o programa novo — ou com o recém-apagado ainda ali — é a tela mentindo
 * sobre o que acabou de acontecer. O ViewModel sobrevive à troca de abas, então sem esta releitura
 * a lista ficaria congelada no estado de quando o app abriu.
 */
@Composable
internal fun ProgramsDestination(
    navController: NavHostController,
    dependencies: TrainerDependencies,
    onCreate: () -> Unit,
    onOpen: (String) -> Unit,
    viewModel: ProgramsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ProgramsViewModel(
                    authRepository = dependencies.authRepository,
                    programRepository = dependencies.programRepository,
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

    // Esta é a tela para a qual se volta depois de salvar um programa, e é aqui que o recibo daquela
    // gravação aparece. Sem ele, salvar e sair seria indistinguível de tocar na seta de voltar.
    SavedConfirmation(
        navController = navController,
        route = TrainerRoutes.WORKOUTS,
        snackbarHostState = snackbarHostState,
        message = stringResource(R.string.trainer_saved),
    )

    ProgramsScreen(
        state = state,
        tabs = trainerTabBar(navController = navController, current = TrainerTab.WORKOUTS),
        snackbarHostState = snackbarHostState,
        actions = ProgramsActions(
            onCreate = onCreate,
            onOpen = { program -> onOpen(program.id) },
            onDelete = viewModel::onDelete,
            onRetry = viewModel::refresh,
        ),
    )
}
