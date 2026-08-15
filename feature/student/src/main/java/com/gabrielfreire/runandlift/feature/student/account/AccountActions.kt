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
     * A gravação deu certo.
     *
     * Separado de [onBack] porque as duas voltam para o mesmo lugar e só uma delas leva o recibo
     * junto — sair pela seta não confirma coisa nenhuma, porque nada foi salvo.
     */
    val onSaved: () -> Unit,
    val onBack: () -> Unit,
)
