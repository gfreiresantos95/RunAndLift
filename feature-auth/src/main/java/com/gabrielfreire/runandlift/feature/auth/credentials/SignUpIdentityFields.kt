package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppPasswordField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.nameSupport
import com.gabrielfreire.runandlift.feature.auth.validation.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.validation.message

/**
 * Nome, e-mail e senha: quem é a pessoa e como ela volta.
 *
 * A regra da senha é dita **na entrada do campo**, e não depois do envio: "mínimo de 8 caracteres"
 * antes de digitar evita o erro que "senha muito curta" explicaria depois.
 */
@Composable
internal fun SignUpIdentityFields(
    state: CredentialsUiState,
    form: SignUpFormState,
    actions: SignUpActions,
    formActions: SignUpFormActions,
    role: ActiveRole?,
    modifier: Modifier = Modifier,
) {
    val enabled = !state.submitting
    val minimum = AuthFormValidation.MIN_PASSWORD_LENGTH

    Column(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = form.name,
            onValueChange = formActions.onNameChange,
            label = stringResource(id = R.string.auth_name),
            errorMessage = form.nameError?.message(),
            supportingText = stringResource(id = role.nameSupport()),
            enabled = enabled,
            keyboardType = KeyboardType.Text,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        AppTextField(
            value = state.email,
            onValueChange = actions.onEmailChange,
            label = stringResource(id = R.string.auth_email),
            errorMessage = state.emailError?.message(),
            enabled = enabled,
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
            // A regra dita na entrada do campo evita o erro que ela descreveria depois do envio.
            supportingText = pluralStringResource(
                id = R.plurals.auth_password_min_length,
                count = minimum,
                minimum,
            ),
            enabled = enabled,
            // `Next`, e não `Done`: a senha deixou de ser o último campo do formulário.
            imeAction = ImeAction.Next,
        )
    }
}

/** Preenchido e com o texto de apoio do treinador, que é o que difere do aluno neste bloco. */
@LightDarkPreviews
@Composable
private fun SignUpIdentityFieldsPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                SignUpIdentityFields(
                    state = previewCredentialsState(),
                    form = previewTrainerForm(),
                    actions = previewSignUpActions(),
                    formActions = previewSignUpFormActions(),
                    role = ActiveRole.TRAINER,
                )
            }
        }
    }
}
