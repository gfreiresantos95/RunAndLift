package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppPasswordField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.message

/**
 * Textos que mudam entre entrar e criar conta. Agrupados porque são um conjunto coeso — passá-los
 * soltos faria a assinatura da tela crescer sem que nenhum deles fizesse sentido isolado.
 */
@Immutable
internal data class CredentialsLabels(val title: String, val submit: String, val alternative: String)

/** Eventos da tela. Agrupados pelo mesmo motivo dos rótulos. */
@Immutable
internal data class CredentialsActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onAlternative: () -> Unit,
    val onAuthenticated: () -> Unit,
    val onGoogleSignIn: () -> Unit,
    val onForgotPassword: (() -> Unit)? = null,
)

/**
 * Formulário de e-mail e senha, usado por entrar e por criar conta.
 *
 * A tela não tem estado próprio: recebe [state] e devolve eventos. É o que a torna previsível na
 * preview e mantém toda a regra em [CredentialsViewModel], onde é testável sem Android.
 */
@Composable
internal fun CredentialsScreen(
    state: CredentialsUiState,
    labels: CredentialsLabels,
    actions: CredentialsActions,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.authenticated) {
        if (state.authenticated) actions.onAuthenticated()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        Text(text = labels.title, style = MaterialTheme.typography.headlineMedium)

        AppTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = stringResource(R.string.auth_email),
            errorMessage = state.emailError?.message(),
            enabled = !state.submitting,
            keyboardType = KeyboardType.Email,
        )

        AppPasswordField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            label = stringResource(R.string.auth_password),
            showLabel = stringResource(R.string.auth_password_show),
            hideLabel = stringResource(R.string.auth_password_hide),
            errorMessage = state.passwordError?.message(),
            enabled = !state.submitting,
            imeAction = ImeAction.Done,
        )

        state.failure?.let { failure -> FailureMessage(failure) }

        AppButton(
            text = labels.submit,
            onClick = actions.onSubmit,
            loading = state.submitting,
        )

        // Secundário e não primário: e-mail e senha é o caminho que funciona para todo mundo,
        // inclusive em aparelho sem conta Google configurada.
        AppOutlinedButton(
            text = stringResource(R.string.auth_google_action),
            onClick = actions.onGoogleSignIn,
            enabled = !state.submitting,
        )

        AppTextButton(
            text = labels.alternative,
            onClick = actions.onAlternative,
            enabled = !state.submitting,
        )

        actions.onForgotPassword?.let { onClick ->
            AppTextButton(
                text = stringResource(R.string.auth_forgot_password),
                onClick = onClick,
                enabled = !state.submitting,
            )
        }
    }
}

/**
 * Falha do servidor, acima do botão e não em snackbar: mensagem que some sozinha é mensagem que o
 * usuário menos digital não chega a ler (D11).
 */
@Composable
private fun FailureMessage(failure: AuthFailure, modifier: Modifier = Modifier) {
    Text(
        text = failure.message(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}
