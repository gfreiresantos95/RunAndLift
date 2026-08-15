package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * A confirmação de que a gravação deu certo, viajando da tela de edição para a tela de volta.
 *
 * Existe por causa de uma tensão real entre duas coisas certas. Sair da tela ao salvar é o que a
 * pessoa espera — ela veio corrigir um dado, corrigiu, e não tem mais o que fazer ali. Mas sair em
 * silêncio torna o salvamento **indistinguível de ter tocado na seta de voltar**: nos dois casos a
 * tela some e nada é dito.
 *
 * A saída é a mesma mecânica que a seleção de estado e cidade já usa — escrever o resultado no
 * `SavedStateHandle` da entrada anterior da pilha antes de desempilhar. Só que aqui o que volta não
 * é uma escolha, é um recibo: a tela de destino o encontra ao recompor e mostra o aviso.
 *
 * **A chave é limpa ao ser lida.** Sem isso, voltar a esta aba por qualquer outro caminho mostraria
 * de novo a confirmação de um salvamento antigo.
 */
internal object SavedResult {

    const val KEY = "student.saved"
}

/**
 * Grava o recibo e volta.
 *
 * A ordem importa, como no seletor de localidade: escrever **antes** de desempilhar. Depois do
 * `popBackStack` esta entrada já saiu, e `previousBackStackEntry` passaria a apontar para outra
 * coisa — ou para nada.
 */
internal fun NavHostController.popWithSavedResult() {
    previousBackStackEntry?.savedStateHandle?.set(SavedResult.KEY, true)
    popBackStack()
}

/**
 * Mostra a confirmação, uma vez, na tela para a qual se voltou.
 *
 * @param route a rota desta tela. A entrada é buscada por ela, e não por `currentBackStackEntry`,
 *   porque durante a animação de volta "a entrada atual" ainda pode ser a tela que está saindo.
 */
@Composable
internal fun SavedConfirmation(
    navController: NavHostController,
    route: String,
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    // `remember` com a entrada atual como chave: buscar a entrada a cada recomposição criaria um
    // observador novo toda vez, e o lint do Navigation cobra isso. A chave é a entrada atual porque
    // é ela que muda quando a pilha muda — que é o único momento em que a busca precisa refazer.
    val currentEntry by navController.currentBackStackEntryAsState()
    val handle = remember(currentEntry) { navController.getBackStackEntry(route) }.savedStateHandle
    val saved by handle.getStateFlow(SavedResult.KEY, false).collectAsStateWithLifecycle()

    LaunchedEffect(saved) {
        if (!saved) return@LaunchedEffect

        handle[SavedResult.KEY] = false
        snackbarHostState.showSnackbar(message = message)
    }
}
