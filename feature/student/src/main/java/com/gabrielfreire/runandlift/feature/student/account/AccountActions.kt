package com.gabrielfreire.runandlift.feature.student.account

import androidx.compose.runtime.Immutable

/**
 * Eventos da tela de dados cadastrais.
 *
 * Reunidos num contrato quando a localidade entrou: com seis lambdas soltas, a assinatura da tela
 * passaria do limite de parâmetros do projeto e — o que custa mais caro — viraria seis lambdas
 * posicionais idênticas para o compilador, onde trocar duas de lugar compila e passa.
 *
 * É o mesmo desenho dos `…Actions` do cadastro e do perfil de treino.
 *
 * @param onOpenStatePicker abre a tela de escolher o estado. É `onOpen…` e não `onStateChange`
 *   porque o campo não muda por digitação: ele abre outra tela e recebe o resultado dela depois.
 */
@Immutable
internal data class AccountActions(
    val onNameChange: (String) -> Unit,
    val onPhoneChange: (String) -> Unit,
    val onOpenStatePicker: () -> Unit,
    val onOpenCityPicker: () -> Unit,
    val onSubmit: () -> Unit,
    /**
     * A confirmação de salvamento já foi exibida.
     *
     * Existe para o sinal de "salvou" ser **um evento**, e não um estado que fica ligado: sem
     * baixá-lo, a segunda gravação não dispararia aviso nenhum, porque o valor já era verdadeiro.
     */
    val onSavedShown: () -> Unit,
    val onBack: () -> Unit,
)
