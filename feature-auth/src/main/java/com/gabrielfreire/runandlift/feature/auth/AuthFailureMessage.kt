package com.gabrielfreire.runandlift.feature.auth

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.auth.AuthFailure

/**
 * A frase que cada falha do servidor vira na tela.
 *
 * É a **única** tradução de enum para texto que não fica junto do seu enum, e a exceção tem motivo:
 * [AuthFailure] mora em `:data`, que não tem recursos de string nem dependência de Compose. Colocar
 * uma função `@Composable` ao lado dele obrigaria a camada de dados a arrastar a UI junto e
 * inverteria a direção dos módulos. Os erros de formulário, que nascem aqui mesmo, seguem a regra
 * normal — cada um com o seu `message()` no arquivo do enum, em `validation/`.
 *
 * A divisão continua valendo: o repositório entrega o **motivo**, a tela escolhe a frase.
 */
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
