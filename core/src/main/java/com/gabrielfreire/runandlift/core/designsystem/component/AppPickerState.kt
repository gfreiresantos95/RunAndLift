package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.runtime.Immutable

/**
 * Em que pé está a lista de uma tela de seleção.
 *
 * Os três casos existem separados porque **lista vazia e lista que não carregou são telas
 * diferentes**: uma diz que não há o que escolher, a outra oferece tentar de novo. Um único
 * `List<String>` para descrever os dois faria a queda de rede aparecer como "nenhum resultado", e
 * quem procurava a própria cidade concluiria que o app não a conhece.
 *
 * [Options] com lista vazia é caso legítimo e distinto de [Failed]: é o que a busca devolve quando
 * o texto digitado não casa com nada.
 */
@Immutable
sealed interface AppPickerState {

    /** Ainda carregando. Nada é desenhado além do indicador — nem lista, nem "nada encontrado". */
    data object Loading : AppPickerState

    /** O que há para escolher, já filtrado pela busca. Vazio significa "a busca não achou nada". */
    data class Options(val items: List<String>) : AppPickerState

    /** Não deu para carregar. A tela oferece nova tentativa em vez de uma lista mentirosa. */
    data object Failed : AppPickerState
}
