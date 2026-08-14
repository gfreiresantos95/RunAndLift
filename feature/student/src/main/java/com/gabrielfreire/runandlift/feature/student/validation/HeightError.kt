package com.gabrielfreire.runandlift.feature.student.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O que pode estar errado na altura.
 *
 * Um caso só, pela mesma razão do peso: o campo é opcional, e o que resta é número que não fecha.
 * O erro mais comum aqui é a unidade — quem digita 1,75 em vez de 175 —, e é para ele que a
 * mensagem aponta.
 */
internal enum class HeightError { INVALID, }

@Composable
internal fun HeightError.message(): String = stringResource(
    when (this) {
        HeightError.INVALID -> R.string.student_error_height_invalid
    },
)
