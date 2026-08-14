package com.gabrielfreire.runandlift.feature.auth.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * O que pode faltar no registro no CREF.
 *
 * Só dois casos porque a máscara `######-A/AA` já barra o resto na digitação. O que sobra é o que
 * ela não tem como saber: se está completo e se a sigla é de um estado que existe.
 */
internal enum class CrefError { REQUIRED, INVALID }

/**
 * [CrefError.INVALID] aponta as três partes e repete o exemplo: "inválido" sozinho deixa a pessoa
 * adivinhando qual delas errou, num campo que ela copia de uma carteira.
 */
@Composable
internal fun CrefError.message(): String = stringResource(
    when (this) {
        CrefError.REQUIRED -> R.string.auth_error_cref_required
        CrefError.INVALID -> R.string.auth_error_cref_invalid
    },
)
