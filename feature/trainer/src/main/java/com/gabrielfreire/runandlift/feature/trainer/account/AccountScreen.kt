package com.gabrielfreire.runandlift.feature.trainer.account

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppMaskedTextField
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppSelectField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.validation.AccountFormValidation
import com.gabrielfreire.runandlift.feature.trainer.validation.message

/**
 * Meus dados — o que a conta guarda sobre a pessoa, não sobre a atuação dela.
 *
 * A ordem é **editável primeiro, fixo depois**. Começar pelos campos travados faria a pessoa rolar
 * por campos que ela não pode mexer para chegar aos que pode; terminar por eles os transforma no
 * que são, uma nota de rodapé que responde "e o meu e-mail?".
 *
 * Estado e cidade **abrem uma tela** em vez de aceitarem digitação, e mostram a escolha na mesma
 * forma em que ela foi feita — `São Paulo - SP`. O banco guarda só a sigla.
 *
 * **Salvar volta para o menu, e o recibo vai junto.** O que não pode é sair em silêncio — assim,
 * salvar ficaria indistinguível de ter tocado na seta de voltar. Ver `SavedResult`.
 */
@Composable
internal fun AccountScreen(state: AccountUiState, actions: AccountActions, modifier: Modifier = Modifier) {
    LaunchedEffect(state.saved) {
        if (state.saved) actions.onSaved()
    }

    AppScreenScaffold(
        title = stringResource(R.string.trainer_account_title),
        modifier = modifier,
        onBack = actions.onBack,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        if (state.loading) {
            AppLoadingState(contentDescription = stringResource(R.string.trainer_loading))
            return@AppScreenScaffold
        }

        AccountFields(state = state, actions = actions)

        if (state.failed) {
            AppMessageCard(text = stringResource(R.string.trainer_save_failed))
        }

        AppButton(
            text = stringResource(R.string.trainer_action_save),
            onClick = actions.onSubmit,
            loading = state.saving,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Os seis campos, na ordem **editável primeiro, fixo depois**.
 *
 * A linha de apoio do celular diz que ele é **necessário**, e não que é opcional como no aluno: é a
 * mesma regra do cadastro, e repeti-la aqui evita que a pessoa descubra a obrigatoriedade só ao
 * tocar em salvar.
 *
 * A cidade fica desabilitada até haver um estado, e a linha de apoio diz por quê. Impedir é melhor
 * que acusar: uma lista de municípios sem estado seriam os 5.571 do país inteiro.
 */
@Composable
private fun AccountFields(state: AccountUiState, actions: AccountActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        AppTextField(
            value = state.name,
            onValueChange = actions.onNameChange,
            label = stringResource(R.string.trainer_field_name),
            supportingText = stringResource(R.string.trainer_field_name_support),
            errorMessage = state.nameError?.message(),
        )

        AppMaskedTextField(
            value = state.phone,
            onValueChange = actions.onPhoneChange,
            label = stringResource(R.string.trainer_field_phone),
            mask = AccountFormValidation.PHONE_MASK,
            supportingText = stringResource(R.string.trainer_field_phone_support),
            errorMessage = state.phoneError?.message(),
            imeAction = ImeAction.Done,
        )

        AppSelectField(
            value = state.selectedState?.label.orEmpty(),
            label = stringResource(R.string.trainer_field_state),
            onClick = actions.onOpenStatePicker,
            supportingText = stringResource(R.string.trainer_field_state_support),
            errorMessage = state.stateError?.message(),
        )

        AppSelectField(
            value = state.city,
            label = stringResource(R.string.trainer_field_city),
            onClick = actions.onOpenCityPicker,
            supportingText = stringResource(
                if (state.selectedState == null) {
                    R.string.trainer_field_city_needs_state
                } else {
                    R.string.trainer_field_city_support
                },
            ),
            errorMessage = state.cityError?.message(),
            enabled = state.selectedState != null,
        )

        AppTextField(
            value = state.email,
            onValueChange = {},
            label = stringResource(R.string.trainer_field_email),
            supportingText = stringResource(R.string.trainer_field_email_support),
            enabled = false,
        )

        AppTextField(
            value = state.birthDate,
            onValueChange = {},
            label = stringResource(R.string.trainer_field_birth_date),
            supportingText = stringResource(R.string.trainer_field_birth_date_support),
            enabled = false,
        )
    }
}

@Preview(name = "Meus dados do treinador · claro", showBackground = true, heightDp = 1180)
@Preview(
    name = "Meus dados do treinador · escuro",
    showBackground = true,
    heightDp = 1180,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AccountScreenPreview() {
    RunAndLiftTheme {
        AccountScreen(
            state = AccountUiState(
                loading = false,
                name = "Carlos Pereira",
                phone = "11987654321",
                email = "carlos@exemplo.com",
                birthDate = "03/11/1988",
                stateUf = "SP",
                stateName = "São Paulo",
                city = "Campinas",
            ),
            actions = previewAccountActions(),
        )
    }
}

/**
 * A conta que ainda não tem localidade — o caso de toda conta criada antes de o campo existir.
 * É o que se confere: a cidade travada precisa parecer "ainda não", e não "quebrado".
 */
@Preview(name = "Meus dados do treinador · sem localidade", showBackground = true, heightDp = 1180)
@Composable
private fun AccountScreenWithoutLocationPreview() {
    RunAndLiftTheme {
        AccountScreen(
            state = AccountUiState(
                loading = false,
                name = "Carlos Pereira",
                email = "carlos@exemplo.com",
                birthDate = "03/11/1988",
            ),
            actions = previewAccountActions(),
        )
    }
}

private fun previewAccountActions() = AccountActions(
    onNameChange = {},
    onPhoneChange = {},
    onOpenStatePicker = {},
    onOpenCityPicker = {},
    onSubmit = {},
    onSaved = {},
    onBack = {},
)
