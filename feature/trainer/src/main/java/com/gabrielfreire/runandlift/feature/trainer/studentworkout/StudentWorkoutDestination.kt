package com.gabrielfreire.runandlift.feature.trainer.studentworkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies

/**
 * Liga o treino do aluno ao seu ViewModel.
 *
 * Relê a cada volta para a tela, e não só na criação: o caminho normal daqui é ir até a aba de
 * treinos, atribuir outro programa e voltar — e voltar encontrando o treino antigo seria a tela
 * mentindo sobre o que acabou de acontecer.
 *
 * O `key` é o identificador do aluno: sem ele, abrir o treino de dois alunos seguidos reaproveitaria
 * o mesmo ViewModel e o segundo abriria com a prescrição do primeiro na tela.
 */
@Composable
internal fun StudentWorkoutDestination(
    dependencies: TrainerDependencies,
    studentId: String,
    onOpenPrograms: () -> Unit,
    onBack: () -> Unit,
    viewModel: StudentWorkoutViewModel = viewModel(
        key = studentId,
        factory = viewModelFactory {
            initializer {
                StudentWorkoutViewModel(
                    authRepository = dependencies.authRepository,
                    assignmentRepository = dependencies.assignmentRepository,
                    studentId = studentId,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(studentId) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    StudentWorkoutScreen(
        state = state,
        actions = StudentWorkoutActions(onOpenPrograms = onOpenPrograms, onRetry = viewModel::refresh),
        onBack = onBack,
    )
}
