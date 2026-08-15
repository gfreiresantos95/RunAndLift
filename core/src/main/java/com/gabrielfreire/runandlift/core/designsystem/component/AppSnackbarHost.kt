package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Onde as confirmações aparecem.
 *
 * **Confirmação é snackbar; falha é [AppMessageCard].** A divisão não é estética: o snackbar some
 * sozinho, e sumir é o comportamento certo para "salvo" — a pessoa já sabe o que fez, e uma
 * mensagem que exige ser dispensada transforma um acerto em tarefa. Para falha ele é o componente
 * errado pela mesma razão: sumiria justamente enquanto se relê o formulário procurando o que
 * corrigir.
 *
 * Existe como componente, e não como o `SnackbarHost` cru, porque o padrão do Material usa
 * `inverseSurface` — uma faixa escura no tema claro e clara no escuro. Funciona, e neste app
 * competiria com o semáforo de aderência, que é o único elemento que deveria puxar o olho por
 * inversão de cor. Aqui a faixa usa `surfaceContainerHighest`, que se destaca do fundo sem gritar.
 *
 * A forma vem do tema, então ela acompanha o arredondamento do resto do app em vez de ter o seu.
 */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            actionColor = MaterialTheme.colorScheme.primary,
        )
    }
}
