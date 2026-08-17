package com.gabrielfreire.runandlift.feature.trainer.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O que pode estar errado na capacidade de atendimento.
 *
 * Um caso só: o campo é opcional, então "vazio" não é erro, e o que sobra é número que não fecha —
 * zero, ou uma quantidade grande demais para ter sido digitada de propósito.
 */
internal enum class CapacityError { INVALID, }

/**
 * A frase fala de **conferir o número**, e não do valor. Quem digitou 200 no lugar de 20 precisa do
 * aviso; quem de fato acompanha 120 pessoas não precisa que o app duvide.
 */
@Composable
internal fun CapacityError.message(): String = stringResource(
    when (this) {
        CapacityError.INVALID -> R.string.trainer_error_capacity_invalid
    },
)
