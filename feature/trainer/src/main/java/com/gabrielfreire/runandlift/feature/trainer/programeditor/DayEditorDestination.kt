package com.gabrielfreire.runandlift.feature.trainer.programeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.trainer.catalog.PickedExerciseEffect
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerRoutes

/**
 * Liga o editor de dia ao ViewModel compartilhado com o editor de programa.
 *
 * **O exercício escolhido no catálogo chega aqui**, pela `SavedStateHandle` desta entrada — e é
 * este o ponto em que ele vira prescrição. O catálogo devolve só o id; quem o transforma em
 * exercício é o repositório, e quem sabe em que dia ele entra é esta tela.
 *
 * A busca do exercício pelo id custa **zero leitura do Firestore**: sai do Room, que é onde o
 * catálogo vive.
 */
@Composable
internal fun DayEditorDestination(
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

    val programId = entry.arguments?.getString(TrainerRoutes.PROGRAM_ID_ARG) ?: TrainerRoutes.NEW_PROGRAM
    val dayIndex = entry.arguments?.getInt(TrainerRoutes.DAY_INDEX_ARG) ?: 0
    val day = state.program.days.getOrNull(dayIndex)

    PickedExerciseEffect(entry = entry) { exerciseId ->
        viewModel.addExerciseFromCatalog(dayIndex = dayIndex, exerciseId = exerciseId)
    }

    // O dia pode não existir mais: o processo pode ter sido recriado com a rota antiga e um programa
    // relido do servidor sem ele. Voltar é a resposta certa — melhor que uma tela que edita o nada.
    if (day == null) {
        onBack()
        return
    }

    DayEditorScreen(
        day = day,
        actions = dayEditorActions(
            navController = navController,
            viewModel = viewModel,
            programId = programId,
            dayIndex = dayIndex,
            onBack = onBack,
        ),
        onBack = onBack,
    )
}

/**
 * As ações do dia, montadas fora do composable para ele não virar uma parede de lambdas.
 *
 * Não é `@Composable` de propósito: nada aqui lê estado de composição, e mantê-la simples é o que
 * permite ler a tela acima de uma vez só.
 */
private fun dayEditorActions(
    navController: NavHostController,
    viewModel: ProgramEditorViewModel,
    programId: String,
    dayIndex: Int,
    onBack: () -> Unit,
): DayEditorActions = DayEditorActions(
    onInfoChange = { label, focus -> viewModel.draft.onDayInfoChange(dayIndex, label, focus) },
    onAddExercise = { navController.navigate(TrainerRoutes.CATALOG) },
    onOpenExercise = { index ->
        navController.navigate(
            TrainerRoutes.prescription(programId = programId, dayIndex = dayIndex, exerciseIndex = index),
        )
    },
    onRemoveExercise = { index -> viewModel.draft.onRemoveExercise(dayIndex, index) },
    onMoveUp = { index -> viewModel.draft.onMoveExercise(dayIndex, index, index - 1) },
    onMoveDown = { index -> viewModel.draft.onMoveExercise(dayIndex, index, index + 1) },
    onRemoveDay = {
        viewModel.draft.onRemoveDay(dayIndex)
        onBack()
    },
)
