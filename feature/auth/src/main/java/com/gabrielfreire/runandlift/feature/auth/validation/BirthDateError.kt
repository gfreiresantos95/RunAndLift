package com.gabrielfreire.runandlift.feature.auth.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * O que pode faltar na data de nascimento.
 *
 * [INCOMPLETE] e [INVALID] são casos separados de propósito: quem está no meio da digitação não
 * merece ouvir que a data não existe. É a diferença entre "continue" e "você errou".
 */
internal enum class BirthDateError { REQUIRED, INCOMPLETE, INVALID, TOO_YOUNG }

/**
 * [BirthDateError.TOO_YOUNG] diz a regra **e** por onde o menor de idade entra: barreira sem saída
 * é porta na cara. O plural existe pelo mesmo motivo do erro de senha.
 */
@Composable
internal fun BirthDateError.message(): String = when (this) {
    BirthDateError.REQUIRED -> stringResource(R.string.auth_error_birth_date_required)

    BirthDateError.INCOMPLETE -> stringResource(R.string.auth_error_birth_date_incomplete)

    BirthDateError.INVALID -> stringResource(R.string.auth_error_birth_date_invalid)

    BirthDateError.TOO_YOUNG -> pluralStringResource(
        R.plurals.auth_error_birth_date_too_young,
        AuthFormValidation.MIN_AGE_YEARS,
        AuthFormValidation.MIN_AGE_YEARS,
    )
}
