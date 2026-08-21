package com.gabrielfreire.runandlift.feature.trainer.programeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerRoutes

/**
 * O [ProgramEditorViewModel] da montagem em curso, seja qual for a tela que pedir.
 *
 * **É a peça que faz as três telas editarem o mesmo programa.** O editor de dia e o de prescrição
 * são empilhados por cima do editor de programa, então a entrada dele continua viva na pilha — e um
 * `ViewModel` pedido com essa entrada como dono é literalmente o mesmo objeto. Sem isto, cada tela
 * teria o próprio rascunho, e ajustar as séries de um exercício não chegaria ao programa que se
 * salva.
 *
 * A alternativa seria gravar no Firestore a cada passo, para as telas se comunicarem pelo servidor:
 * uma leitura por dia aberto e uma escrita por número ajustado. É exatamente o que o orçamento de
 * leitura (§2.4) existe para evitar.
 *
 * O `remember` tem a entrada como chave porque `getBackStackEntry` percorre a pilha, e refazer isso
 * a cada recomposição é o que o lint do Navigation cobra — a mesma razão do `remember` em
 * `SavedConfirmation`.
 *
 * @param entry a entrada da tela que está pedindo. É dela que se sobe até a do editor.
 */
@Composable
internal fun sharedProgramEditorViewModel(
    navController: NavHostController,
    entry: NavBackStackEntry,
    dependencies: TrainerDependencies,
): ProgramEditorViewModel {
    val programId = entry.arguments?.getString(TrainerRoutes.PROGRAM_ID_ARG) ?: TrainerRoutes.NEW_PROGRAM
    val editorEntry = remember(entry) {
        navController.getBackStackEntry(TrainerRoutes.programEditor(programId))
    }

    return viewModel(
        viewModelStoreOwner = editorEntry,
        factory = viewModelFactory {
            initializer {
                ProgramEditorViewModel(
                    authRepository = dependencies.authRepository,
                    programRepository = dependencies.programRepository,
                    exerciseRepository = dependencies.exerciseRepository,
                    programId = programId,
                )
            }
        },
    )
}
