package com.gabrielfreire.runandlift.feature.trainer.assign

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies

/**
 * Liga a tela de atribuição ao seu ViewModel.
 *
 * A tela **não desempilha ao atribuir**, ao contrário do editor: atribuir o mesmo programa a três
 * alunos é o caso comum, e sair a cada um obrigaria a reabrir a lista três vezes. O recibo é a
 * própria linha, que passa a dizer "está com este treino".
 */
@Composable
internal fun AssignDestination(
    dependencies: TrainerDependencies,
    programId: String,
    onBack: () -> Unit,
    viewModel: AssignViewModel = viewModel(
        key = programId,
        factory = viewModelFactory {
            initializer {
                AssignViewModel(
                    authRepository = dependencies.authRepository,
                    linkRepository = dependencies.linkRepository,
                    programRepository = dependencies.programRepository,
                    assignmentRepository = dependencies.assignmentRepository,
                    programId = programId,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AssignScreen(
        state = state,
        actions = AssignActions(
            onAssign = viewModel::onAssign,
            onRemove = viewModel::onRemove,
            onRetry = viewModel::refresh,
        ),
        onBack = onBack,
    )
}
