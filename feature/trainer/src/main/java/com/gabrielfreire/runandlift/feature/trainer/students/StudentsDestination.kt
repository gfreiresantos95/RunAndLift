package com.gabrielfreire.runandlift.feature.trainer.students

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.trainerTabBar

/**
 * Liga a carteira ao seu ViewModel e às abas.
 *
 * Relê a cada volta para a aba (`LifecycleResumeEffect`), e não só na criação — e aqui isso pesa
 * mais do que na home: **o que muda nesta tela é ação de outra pessoa**. Um aluno digita o código
 * agora, e o pedido tem de estar ali quando o treinador voltar da aba ao lado. O ViewModel sobrevive
 * à troca de abas, então sem esta releitura a carteira ficaria congelada no estado de quando o app
 * abriu.
 */
@Composable
internal fun StudentsDestination(
    navController: NavHostController,
    dependencies: TrainerDependencies,
    onOpenInvite: () -> Unit,
    viewModel: StudentsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                StudentsViewModel(
                    authRepository = dependencies.authRepository,
                    linkRepository = dependencies.linkRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    StudentsScreen(
        state = state,
        tabs = trainerTabBar(navController = navController, current = TrainerTab.STUDENTS),
        actions = StudentsActions(
            onOpenInvite = onOpenInvite,
            onStatusChange = viewModel::onStatusChange,
            onRetry = viewModel::refresh,
        ),
    )
}
