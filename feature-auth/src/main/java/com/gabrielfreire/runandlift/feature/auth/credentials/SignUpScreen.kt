package com.gabrielfreire.runandlift.feature.auth.credentials

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppPasswordField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.message

/**
 * Criar conta.
 *
 * Tela própria, e não a de entrar com outros rótulos: o que ela pede é o mesmo, o que ela promete
 * não é. Aqui a senha anuncia a regra antes do envio, a frase de apoio descreve o que o perfil
 * escolhido vai receber, e não existe recuperação de senha.
 *
 * O título é sempre o mesmo — quem carrega o perfil é a etiqueta, não a manchete. Repetir "de
 * aluno" no título com o chip logo acima dizendo "Aluno" seria dizer a mesma coisa duas vezes.
 *
 * @param role perfil escolhido na abertura. Quando é `null`, o cadastro veio da tela de entrar e a
 *   escolha de papel acontece depois de autenticar.
 */
@Composable
internal fun SignUpScreen(
    state: CredentialsUiState,
    actions: SignUpActions,
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
                prompt = stringResource(R.string.auth_prompt_has_account),
                action = stringResource(R.string.auth_go_to_sign_in),
                onClick = actions.onSignIn,
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
            title = stringResource(R.string.auth_sign_up_title),
            subtitle = stringResource(role.subtitle()),
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceXLarge))

        SignUpForm(state = state, actions = actions)
    }
}

@Composable
private fun SignUpForm(state: CredentialsUiState, actions: SignUpActions, modifier: Modifier = Modifier) {
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
            // A regra dita na entrada do campo evita o erro que ela descreveria depois do envio.
            supportingText = pluralStringResource(
                R.plurals.auth_password_min_length,
                AuthFormValidation.MIN_PASSWORD_LENGTH,
                AuthFormValidation.MIN_PASSWORD_LENGTH,
            ),
            enabled = !state.submitting,
            imeAction = ImeAction.Done,
            onImeAction = actions.onSubmit,
        )

        state.failure?.let { failure ->
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            FailureBanner(failure = failure)
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceXLarge))

        AppButton(
            text = stringResource(R.string.auth_sign_up_action),
            onClick = actions.onSubmit,
            loading = state.submitting,
        )
    }
}

/** O que a conta vai fazer, na voz de quem vai usá-la — é a promessa que justifica pedir os dados. */
@StringRes
private fun ActiveRole?.subtitle(): Int = when (this) {
    ActiveRole.STUDENT -> R.string.auth_sign_up_subtitle_student
    ActiveRole.TRAINER -> R.string.auth_sign_up_subtitle_trainer
    null -> R.string.auth_sign_up_subtitle
}

@Preview(name = "Criar conta · claro", showBackground = true, heightDp = 900)
@Preview(
    name = "Criar conta · escuro",
    showBackground = true,
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SignUpPreview() {
    RunAndLiftTheme {
        SignUpScreen(
            state = CredentialsUiState(email = "ana@exemplo.com", password = "123456"),
            actions = previewSignUpActions(),
            role = ActiveRole.TRAINER,
        )
    }
}

@Preview(name = "Criar conta · falha", showBackground = true, heightDp = 900)
@Composable
private fun SignUpFailurePreview() {
    RunAndLiftTheme {
        SignUpScreen(
            state = CredentialsUiState(
                email = "ana@exemplo.com",
                failure = AuthFailure.EMAIL_ALREADY_IN_USE,
            ),
            actions = previewSignUpActions(),
            role = ActiveRole.STUDENT,
        )
    }
}

private fun previewSignUpActions() = SignUpActions(
    onEmailChange = {},
    onPasswordChange = {},
    onSubmit = {},
    onGoogleSignIn = {},
    onSignIn = {},
    onBack = {},
    onAuthenticated = {},
)
