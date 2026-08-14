package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.runtime.Immutable

/**
 * O que uma tela de seleção pode disparar.
 *
 * Mesmo desenho dos `…Actions` das telas de cadastro: um contrato só, para que nenhuma lambda seja
 * esquecida na hora de ligar a tela — quatro parâmetros soltos numa assinatura que já recebe textos
 * e estado passariam do limite do projeto e, pior, virariam quatro lambdas posicionais idênticas
 * para o compilador.
 *
 * @param onSelect o item escolhido, pelo texto exibido. É o texto e não um índice porque a lista
 *   que a tela mostra é a **filtrada**, e um índice sobre ela não significa nada para quem chamou.
 * @param onRetry nova tentativa depois de falha. Só é alcançável no estado [AppPickerState.Failed].
 */
@Immutable
data class AppPickerActions(
    val onQueryChange: (String) -> Unit,
    val onSelect: (String) -> Unit,
    val onRetry: () -> Unit,
    val onBack: () -> Unit,
)
