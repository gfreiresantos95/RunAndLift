package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppPasswordField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.component.FailureBanner
import com.gabrielfreire.runandlift.feature.auth.component.GoogleSignInButton
import com.gabrielfreire.runandlift.feature.auth.component.LegalLinks
import com.gabrielfreire.runandlift.feature.auth.component.OrSeparator
import com.gabrielfreire.runandlift.feature.auth.validation.message

/**
 * Os dois campos da entrada, os dois caminhos de autenticação e o aviso legal que o segundo exige.
 *
 * **Não há regra de senha aqui**, ao contrário do cadastro: anunciar "mínimo de 8 caracteres" a
 * quem está entrando revelaria a regra a quem tem senha antiga mais curta, e não evitaria erro
 * nenhum — a senha certa já existe.
 */
@Composable
internal fun SignInForm(state: CredentialsUiState, actions: SignInActions, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = stringResource(id = R.string.auth_email),
            errorMessage = state.emailError?.message(),
            enabled = !state.submitting,
            keyboardType = KeyboardType.Email,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        AppPasswordField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            label = stringResource(id = R.string.auth_password),
            showLabel = stringResource(id = R.string.auth_password_show),
            hideLabel = stringResource(id = R.string.auth_password_hide),
            errorMessage = state.passwordError?.message(),
            enabled = !state.submitting,
            imeAction = ImeAction.Done,
            // Enviar pela tecla do teclado: com o teclado aberto, o botão está atrás dele.
            onImeAction = actions.onSubmit,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))

        // Colado na senha e alinhado à direita, que é onde se procura por isso depois de errá-la —
        // e não no fim da tela, junto de ações sem relação com ela.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            AppTextButton(
                text = stringResource(id = R.string.auth_forgot_password),
                onClick = actions.onForgotPassword,
                enabled = !state.submitting,
            )
        }

        state.failure?.let { failure ->
            Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
            FailureBanner(failure = failure)
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        AppButton(
            text = stringResource(id = R.string.auth_sign_in_action),
            onClick = actions.onSubmit,
            loading = state.submitting,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        OrSeparator()

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        GoogleSignInButton(onClick = actions.onGoogleSignIn, enabled = !state.submitting)

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        // A folha do Google entra e **cadastra** pela mesma porta: quem chega aqui sem conta sai
        // daqui com uma. Dizer a que se está concordando é obrigação de quem cria a conta, e não
        // do formulário de cadastro — que esta pessoa não vai ver.
        Text(
            text = stringResource(id = R.string.auth_legal_notice_sign_in),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceXSmall))

        LegalLinks(onOpen = actions.onOpenLegalDocument, enabled = !state.submitting)
    }
}

/** Enviando: é o estado em que tudo desabilita de uma vez, e o único que exercita isso. */
@LightDarkPreviews
@Composable
private fun SignInFormPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                SignInForm(
                    state = previewCredentialsState().copy(submitting = true),
                    actions = previewSignInActions(),
                )
            }
        }
    }
}
