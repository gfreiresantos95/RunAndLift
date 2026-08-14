package com.gabrielfreire.runandlift.feature.student.account

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppMaskedTextField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTopBar
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.validation.AccountFormValidation
import com.gabrielfreire.runandlift.feature.student.validation.message

/**
 * Meus dados — o que a conta guarda sobre a pessoa, não sobre o treino.
 *
 * A ordem é **editável primeiro, fixo depois**. Começar pelos campos travados faria a pessoa rolar
 * por dois campos que ela não pode mexer para chegar aos dois que pode; terminar por eles os
 * transforma no que são, uma nota de rodapé que responde "e o meu e-mail?".
 *
 * Os dois campos travados vêm com a explicação embaixo, e não apenas apagados: um campo cinza sem
 * motivo parece defeito, e a pergunta que ele levanta — "por que não posso mudar?" — é exatamente a
 * que vira mensagem de suporte.
 */
@Composable
internal fun AccountScreen(
    state: AccountUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.student_account_title),
                onBack = onBack,
                backContentDescription = stringResource(R.string.student_action_back),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .padding(paddingValues = Dimens.ScreenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
        ) {
            if (state.loading) return@Column

            AccountFields(state = state, onNameChange = onNameChange, onPhoneChange = onPhoneChange)

            if (state.failed) {
                Text(
                    text = stringResource(R.string.student_save_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            AppButton(
                text = stringResource(R.string.student_action_save),
                onClick = onSubmit,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Os quatro campos, na ordem **editável primeiro, fixo depois**.
 *
 * Os dois travados vêm com a explicação embaixo, e não apenas apagados: um campo cinza sem motivo
 * parece defeito, e a pergunta que ele levanta — "por que não posso mudar?" — é exatamente a que
 * vira mensagem de suporte.
 */
@Composable
private fun AccountFields(
    state: AccountUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        AppTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.student_field_name),
            supportingText = stringResource(R.string.student_field_name_support),
            errorMessage = state.nameError?.message(),
        )

        AppMaskedTextField(
            value = state.phone,
            onValueChange = onPhoneChange,
            label = stringResource(R.string.student_field_phone),
            mask = AccountFormValidation.PHONE_MASK,
            supportingText = stringResource(R.string.student_field_phone_support),
            errorMessage = state.phoneError?.message(),
        )

        AppTextField(
            value = state.email,
            onValueChange = {},
            label = stringResource(R.string.student_field_email),
            supportingText = stringResource(R.string.student_field_email_support),
            enabled = false,
        )

        AppTextField(
            value = state.birthDate,
            onValueChange = {},
            label = stringResource(R.string.student_field_birth_date),
            supportingText = stringResource(R.string.student_field_birth_date_support),
            enabled = false,
        )
    }
}

@Preview(name = "Meus dados · claro", showBackground = true, heightDp = 860)
@Preview(
    name = "Meus dados · escuro",
    showBackground = true,
    heightDp = 860,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AccountScreenPreview() {
    RunAndLiftTheme {
        AccountScreen(
            state = AccountUiState(
                loading = false,
                name = "Ana Ribeiro",
                phone = "11987654321",
                email = "ana@exemplo.com",
                birthDate = "21/05/1990",
            ),
            onNameChange = {},
            onPhoneChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}
