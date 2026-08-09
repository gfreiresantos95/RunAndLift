package com.gabrielfreire.runandlift.feature.auth.credentials

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppPasswordField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.message

/**
 * Entrar em conta existente.
 *
 * Tela própria, e não a mesma do cadastro com rótulos trocados: os dois fluxos divergem no que
 * pedem e no que prometem. Aqui existe "esqueci minha senha" e **não** existe regra de senha —
 * anunciar o tamanho mínimo ao entrar revelaria a regra a quem tem senha antiga mais curta.
 *
 * É também **a única porta do cadastro**: o "Ainda não tem conta? Crie uma conta" do rodapé é o
 * único caminho para o formulário de criação, e leva o perfil escolhido na abertura junto. Por
 * isso ele é um botão de texto inteiro, e não uma frase com uma palavra clicável no fim.
 *
 * @param role perfil de onde a pessoa veio, exibido como etiqueta. É o **caminho** que ela
 *   escolheu na abertura, não uma afirmação sobre a conta: quem entra com uma conta de treinador
 *   pelo caminho de aluno vai para a área de treinador, porque o papel real vem do `users/{uid}`.
 */
@Composable
internal fun SignInScreen(
    state: CredentialsUiState,
    actions: SignInActions,
    modifier: Modifier = Modifier,
    role: ActiveRole? = null,
) {
    LaunchedEffect(state.authenticated) {
        if (state.authenticated) actions.onAuthenticated()
    }

    AuthScreenLayout(
        modifier = modifier,
        onBack = actions.onBack,
        bottom = {
            AlternativePrompt(
                prompt = stringResource(R.string.auth_prompt_no_account),
                action = stringResource(R.string.auth_go_to_sign_up),
                onClick = actions.onCreateAccount,
                enabled = !state.submitting,
            )
        },
    ) {
        role?.let {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                RoleChip(role = it)
                Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
            }
        }

        AuthHeadline(
            title = stringResource(R.string.auth_sign_in_title),
            subtitle = stringResource(R.string.auth_sign_in_subtitle),
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceXLarge))

        SignInForm(state = state, actions = actions)
    }
}

@Composable
private fun SignInForm(state: CredentialsUiState, actions: SignInActions, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = stringResource(R.string.auth_email),
            errorMessage = state.emailError?.message(),
            enabled = !state.submitting,
            keyboardType = KeyboardType.Email,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        AppPasswordField(
            value = state.password,
            onValueChange = actions.onPasswordChange,
            label = stringResource(R.string.auth_password),
            showLabel = stringResource(R.string.auth_password_show),
            hideLabel = stringResource(R.string.auth_password_hide),
            errorMessage = state.passwordError?.message(),
            enabled = !state.submitting,
            imeAction = ImeAction.Done,
            // Enviar pela tecla do teclado: com o teclado aberto, o botão está atrás dele.
            onImeAction = actions.onSubmit,
        )

        // Colado na senha e alinhado à direita, que é onde se procura por isso depois de errá-la —
        // e não no fim da tela, junto de ações sem relação com ela.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            AppTextButton(
                text = stringResource(R.string.auth_forgot_password),
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
            text = stringResource(R.string.auth_sign_in_action),
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
            text = stringResource(R.string.auth_legal_notice_sign_in),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        LegalLinks(onOpen = actions.onOpenLegalDocument, enabled = !state.submitting)
    }
}

@Preview(name = "Entrar · claro", showBackground = true, heightDp = 900)
@Preview(
    name = "Entrar · escuro",
    showBackground = true,
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SignInPreview() {
    RunAndLiftTheme {
        SignInScreen(
            state = CredentialsUiState(email = "ana@exemplo.com", password = "123456"),
            actions = previewSignInActions(),
            role = ActiveRole.STUDENT,
        )
    }
}

@Preview(name = "Entrar · falha", showBackground = true, heightDp = 900)
@Composable
private fun SignInFailurePreview() {
    RunAndLiftTheme {
        SignInScreen(
            state = CredentialsUiState(
                email = "ana@exemplo.com",
                password = "123456",
                failure = AuthFailure.INVALID_CREDENTIALS,
            ),
            actions = previewSignInActions(),
            role = ActiveRole.TRAINER,
        )
    }
}

private fun previewSignInActions() = SignInActions(
    onEmailChange = {},
    onPasswordChange = {},
    onSubmit = {},
    onForgotPassword = {},
    onGoogleSignIn = {},
    onCreateAccount = {},
    onBack = {},
    onAuthenticated = {},
    onOpenLegalDocument = {},
)
