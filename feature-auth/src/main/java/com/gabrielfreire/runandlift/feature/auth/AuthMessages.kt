package com.gabrielfreire.runandlift.feature.auth

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.auth.AuthFailure

/**
 * Tradução de erro para texto.
 *
 * Fica na camada de UI, e não no ViewModel, porque o ViewModel não deve conhecer recurso de
 * string — é o que permite testá-lo sem Android. Ele expõe o motivo; a tela escolhe a frase.
 */
@Composable
internal fun EmailError.message(): String = stringResource(
    when (this) {
        EmailError.REQUIRED -> R.string.auth_error_email_required
        EmailError.INVALID -> R.string.auth_error_email_invalid
    },
)

@Composable
internal fun PasswordError.message(): String = when (this) {
    PasswordError.REQUIRED -> stringResource(R.string.auth_error_password_required)

    PasswordError.TOO_SHORT -> pluralStringResource(
        R.plurals.auth_error_password_too_short,
        AuthFormValidation.MIN_PASSWORD_LENGTH,
        AuthFormValidation.MIN_PASSWORD_LENGTH,
    )
}

@Composable
internal fun AuthFailure.message(): String = stringResource(messageRes())

@StringRes
private fun AuthFailure.messageRes(): Int = when (this) {
    AuthFailure.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
    AuthFailure.EMAIL_ALREADY_IN_USE -> R.string.auth_error_email_in_use
    AuthFailure.WEAK_PASSWORD -> R.string.auth_error_weak_password
    AuthFailure.INVALID_EMAIL -> R.string.auth_error_email_invalid
    AuthFailure.NO_NETWORK -> R.string.auth_error_no_network
    AuthFailure.TOO_MANY_ATTEMPTS -> R.string.auth_error_too_many_attempts
    AuthFailure.NO_GOOGLE_ACCOUNT -> R.string.auth_error_no_google_account
    AuthFailure.NOT_SIGNED_IN, AuthFailure.UNKNOWN -> R.string.auth_error_unknown
}
