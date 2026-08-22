package com.gabrielfreire.runandlift.feature.student.workouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.navigation.StudentRoutes

/**
 * O [StudentWorkoutsViewModel] da aba, seja qual for a tela que pedir.
 *
 * **É a peça que faz o dia não custar leitura nenhuma.** O dia é empilhado sobre a aba, então a
 * entrada dela continua viva — e um `ViewModel` pedido com essa entrada como dono é literalmente o
 * mesmo objeto, com a prescrição que já foi lida. Sem isto, cada dia aberto seria uma consulta a
 * `assignments`, e abrir os seis dias de um programa custaria seis leituras para mostrar o conteúdo
 * de um único documento.
 *
 * É a mesma peça do `sharedProgramEditorViewModel` do lado do treinador, e pela mesma razão. A
 * diferença é o que se compartilha: lá é um rascunho que ainda não existe no servidor, aqui é uma
 * leitura que não vale a pena repetir.
 *
 * O `remember` tem a entrada como chave porque `getBackStackEntry` percorre a pilha, e refazer isso
 * a cada recomposição é o que o lint do Navigation cobra.
 *
 * @param entry a entrada da tela que está pedindo. É dela que se sobe até a da aba.
 */
@Composable
internal fun sharedStudentWorkoutsViewModel(
    navController: NavHostController,
    entry: NavBackStackEntry,
    dependencies: StudentDependencies,
): StudentWorkoutsViewModel {
    val tabEntry = remember(entry) { navController.getBackStackEntry(StudentRoutes.WORKOUTS) }

    return viewModel(
        viewModelStoreOwner = tabEntry,
        factory = viewModelFactory {
            initializer {
                StudentWorkoutsViewModel(
                    authRepository = dependencies.authRepository,
                    assignmentRepository = dependencies.assignmentRepository,
                )
            }
        },
    )
}
