package com.gabrielfreire.runandlift.feature.auth.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * O que pode faltar no celular.
 *
 * [REQUIRED] só acontece no cadastro de treinador — para o aluno o campo é opcional. A régua é uma
 * só e mora em [AuthFormValidation.validatePhone]; este enum apenas nomeia o desfecho.
 */
internal enum class PhoneError { REQUIRED, INVALID }

@Composable
internal fun PhoneError.message(): String = stringResource(
    when (this) {
        PhoneError.REQUIRED -> R.string.auth_error_phone_required
        PhoneError.INVALID -> R.string.auth_error_phone_invalid
    },
)
