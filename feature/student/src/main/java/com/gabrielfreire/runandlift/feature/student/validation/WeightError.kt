package com.gabrielfreire.runandlift.feature.student.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O que pode estar errado no peso.
 *
 * Um caso só: o campo é opcional, então "vazio" não é erro, e o que sobra é número que não fecha —
 * texto que não é número, ou fora de uma faixa larga demais para ser julgamento sobre o corpo de
 * alguém.
 */
internal enum class WeightError { INVALID, }

/**
 * A frase fala de **conferir o número**, e não do valor. Quem digitou 7 em vez de 70 precisa do
 * aviso; quem pesa 140 não precisa que o app tenha opinião.
 */
@Composable
internal fun WeightError.message(): String = stringResource(
    when (this) {
        WeightError.INVALID -> R.string.student_error_weight_invalid
    },
)
