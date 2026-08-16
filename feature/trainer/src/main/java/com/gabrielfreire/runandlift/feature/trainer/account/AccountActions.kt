package com.gabrielfreire.runandlift.feature.trainer.account

import androidx.compose.runtime.Immutable

/**
 * Eventos da tela de dados cadastrais.
 *
 * Reunidos num contrato porque com seis lambdas soltas a assinatura da tela passaria do limite de
 * parâmetros do projeto e — o que custa mais caro — viraria seis lambdas posicionais idênticas para
 * o compilador, onde trocar duas de lugar compila e passa.
 *
 * @param onOpenStatePicker abre a tela de escolher o estado. É `onOpen…` e não `onStateChange`
 *   porque o campo não muda por digitação: ele abre outra tela e recebe o resultado dela depois.
 * @param onSaved a gravação deu certo. Separado de [onBack] porque as duas voltam para o mesmo
 *   lugar e só uma delas leva o recibo junto.
 */
@Immutable
internal data class AccountActions(
    val onNameChange: (String) -> Unit,
    val onPhoneChange: (String) -> Unit,
    val onOpenStatePicker: () -> Unit,
    val onOpenCityPicker: () -> Unit,
    val onSubmit: () -> Unit,
    val onSaved: () -> Unit,
    val onBack: () -> Unit,
)
