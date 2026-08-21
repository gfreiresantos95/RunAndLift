package com.gabrielfreire.runandlift.feature.trainer.programeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerRoutes

/**
 * Liga a prescrição ao ViewModel compartilhado.
 *
 * **Cada ajuste vai direto para o rascunho**, sem botão de confirmar — o botão do rodapé só volta.
 * É o comportamento que o contador pede: quem toca em "+" quatro vezes já decidiu, e um "confirmar"
 * depois disso só cria a chance de perder o trabalho ao usar a seta de voltar do aparelho.
 *
 * O que grava no Firestore continua sendo o botão de salvar do editor de programa, uma tela atrás.
 */
@Composable
internal fun PrescriptionDestination(
    navController: NavHostController,
    entry: NavBackStackEntry,
    dependencies: TrainerDependencies,
    onBack: () -> Unit,
) {
    val viewModel = sharedProgramEditorViewModel(
        navController = navController,
        entry = entry,
        dependencies = dependencies,
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val dayIndex = entry.arguments?.getInt(TrainerRoutes.DAY_INDEX_ARG) ?: 0
    val exerciseIndex = entry.arguments?.getInt(TrainerRoutes.EXERCISE_INDEX_ARG) ?: 0
    val exercise = state.program.days.getOrNull(dayIndex)?.exercises?.getOrNull(exerciseIndex)

    // O exercício pode ter deixado de existir — processo recriado com a rota antiga, ou o dia
    // removido de outra tela. Voltar é a resposta certa; editar o nada não é.
    if (exercise == null) {
        onBack()
        return
    }

    PrescriptionScreen(
        exercise = exercise,
        onChange = { updated ->
            viewModel.draft.onPrescriptionChange(
                dayIndex = dayIndex,
                exerciseIndex = exerciseIndex,
                prescription = updated,
            )
        },
        onDone = onBack,
    )
}
