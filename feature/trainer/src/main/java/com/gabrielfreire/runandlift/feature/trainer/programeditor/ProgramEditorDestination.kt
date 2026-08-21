package com.gabrielfreire.runandlift.feature.trainer.programeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerRoutes
import com.gabrielfreire.runandlift.feature.trainer.navigation.popWithSavedResult

/**
 * Liga o editor de programa ao ViewModel compartilhado.
 *
 * **Salvar volta para a lista, levando o recibo** — a mesma mecânica de "Meus dados" e do perfil
 * profissional (`popWithSavedResult`). É o que a pessoa espera: ela veio montar, montou, e a
 * confirmação aparece na aba de treinos com o programa já na lista.
 *
 * A primeira versão desta tela **não** fechava ao salvar, e trocava a rota `novo` pela rota do
 * programa recém-criado para manter o id. Duas coisas davam errado: a troca de rota destruía a
 * entrada antiga e criava outra, que relia o documento do Firestore — o que aparecia como um
 * piscar —, e no fim a tela ficava aberta exatamente onde a pessoa acabara de sair. Desempilhar
 * resolve as duas de uma vez, e não precisa do id de volta para navegação nenhuma.
 */
@Composable
internal fun ProgramEditorDestination(
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

    ProgramEditorScreen(
        state = state,
        actions = ProgramEditorActions(
            onNameChange = viewModel.draft::onNameChange,
            onGoalChange = viewModel.draft::onGoalChange,
            onNotesChange = viewModel.draft::onNotesChange,
            onAddDay = viewModel.draft::onAddDay,
            onOpenDay = { index ->
                navController.navigate(TrainerRoutes.dayEditor(programId = programId, dayIndex = index))
            },
            onRemoveDay = viewModel.draft::onRemoveDay,
            onSave = { viewModel.save { navController.popWithSavedResult() } },
            onAssign = { navController.navigate(TrainerRoutes.assign(programId)) },
        ),
        onBack = onBack,
    )
}
