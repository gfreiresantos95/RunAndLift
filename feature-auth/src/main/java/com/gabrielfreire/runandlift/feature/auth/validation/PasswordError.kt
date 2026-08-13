package com.gabrielfreire.runandlift.feature.auth.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.auth.R

/** O que pode faltar na senha. "Curta demais" só existe no cadastro — ver [AuthFormValidation]. */
internal enum class PasswordError { REQUIRED, TOO_SHORT }

/**
 * Plural, e não uma string com `%d`: no dia em que o mínimo for 1, "1 caracteres" apareceria na
 * tela. O número vem de [AuthFormValidation.MIN_PASSWORD_LENGTH] e não de uma constante repetida
 * aqui — a mensagem e a regra que ela descreve precisam mudar juntas.
 */
@Composable
internal fun PasswordError.message(): String = when (this) {
    PasswordError.REQUIRED -> stringResource(R.string.auth_error_password_required)

    PasswordError.TOO_SHORT -> pluralStringResource(
        R.plurals.auth_error_password_too_short,
        AuthFormValidation.MIN_PASSWORD_LENGTH,
        AuthFormValidation.MIN_PASSWORD_LENGTH,
    )
}
