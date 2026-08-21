package com.gabrielfreire.runandlift.feature.trainer.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

/**
 * O exercício escolhido no catálogo voltando para o dia que o pediu.
 *
 * É o mesmo "start for result" que as listas de estado e cidade já usam: quem escolhe grava o
 * resultado na `SavedStateHandle` da entrada anterior da pilha e desempilha; quem abriu o encontra
 * ao recompor. O catálogo, assim, **não sabe em que dia o exercício entra** — nem precisa —, o que o
 * deixa servir também à navegação livre a partir do menu.
 *
 * A chave é limpa ao ser lida. Sem isso, voltar a esta tela por qualquer outro caminho adicionaria o
 * mesmo exercício de novo.
 */
internal object PickedExercise {

    const val KEY = "trainer.pickedExercise"
}

/**
 * Grava o id escolhido e volta.
 *
 * A ordem importa, como em `popWithSavedResult`: escrever **antes** de desempilhar. Depois do
 * `popBackStack` esta entrada já saiu, e `previousBackStackEntry` apontaria para outra coisa.
 */
internal fun NavHostController.popWithPickedExercise(exerciseId: String) {
    previousBackStackEntry?.savedStateHandle?.set(PickedExercise.KEY, exerciseId)
    popBackStack()
}

/**
 * Entrega o exercício escolhido, uma vez, à tela que abriu o catálogo.
 *
 * @param entry a entrada desta tela. É nela que o catálogo gravou, e é dela que se lê.
 */
@Composable
internal fun PickedExerciseEffect(entry: NavBackStackEntry, onPicked: (String) -> Unit) {
    val handle = remember(entry) { entry.savedStateHandle }
    val picked by handle.getStateFlow<String?>(PickedExercise.KEY, null).collectAsStateWithLifecycle()

    LaunchedEffect(picked) {
        val exerciseId = picked ?: return@LaunchedEffect

        handle[PickedExercise.KEY] = null
        onPicked(exerciseId)
    }
}
