package com.gabrielfreire.runandlift.feature.auth.recovery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppNoticeCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.component.FailureBanner
import com.gabrielfreire.runandlift.feature.auth.validation.message

/**
 * O campo, a confirmação e o botão da recuperação de senha.
 *
 * **A confirmação e a falha são coisas diferentes e aparecem diferentes.** A confirmação é um
 * `AppNoticeCard` neutro, e a falha é o [FailureBanner] vermelho do resto do fluxo. As duas nunca
 * aparecem juntas: o ViewModel zera uma ao publicar a outra.
 *
 * A confirmação **não** é pintada de verde nem carrega ícone de sucesso, e isso é deliberado: ela
 * não afirma que o e-mail existe — afirma que, *se* existir, o link foi enviado. Um selo de sucesso
 * prometeria a certeza que o texto passa a frase inteira evitando dar, e que é justamente o que
 * transformaria a tela num verificador de quem tem conta.
 *
 * O envio também sai pela tecla do teclado: com o teclado aberto num aparelho pequeno, o botão está
 * atrás dele — e este é um formulário de campo único, onde "concluir" é a única continuação
 * possível.
 */
@Composable
internal fun PasswordRecoveryForm(
    state: PasswordRecoveryUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AppTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(id = R.string.auth_email),
            errorMessage = state.emailError?.message(),
            enabled = !state.submitting,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            onImeAction = onSubmit,
        )

        if (state.sent) {
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            AppNoticeCard(text = stringResource(id = R.string.auth_recovery_sent))
        }

        state.failure?.let { failure ->
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            FailureBanner(failure = failure)
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        AppButton(
            text = stringResource(id = R.string.auth_recovery_action),
            onClick = onSubmit,
            loading = state.submitting,
        )
    }
}

/**
 * Os dois desfechos um sob o outro: enviado e falhou. É aqui que se confere que eles não se
 * parecem — se a confirmação e o erro lerem igual, a tela deixa de dizer o que aconteceu.
 */
@LightDarkPreviews
@Composable
private fun PasswordRecoveryFormPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                PasswordRecoveryForm(
                    state = PasswordRecoveryUiState(email = "ana@exemplo.com", sent = true),
                    onEmailChange = {},
                    onSubmit = {},
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceXLarge))

                PasswordRecoveryForm(
                    state = PasswordRecoveryUiState(
                        email = "ana@exemplo.com",
                        failure = AuthFailure.NO_NETWORK,
                    ),
                    onEmailChange = {},
                    onSubmit = {},
                )
            }
        }
    }
}
