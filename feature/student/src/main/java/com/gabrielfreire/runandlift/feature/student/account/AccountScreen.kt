package com.gabrielfreire.runandlift.feature.student.account

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.validation.AccountFormValidation
import com.gabrielfreire.runandlift.feature.student.validation.message

/**
 * Meus dados — o que a conta guarda sobre a pessoa, não sobre o treino.
 *
 * A ordem é **editável primeiro, fixo depois**. Começar pelos campos travados faria a pessoa rolar
 * por campos que ela não pode mexer para chegar aos que pode; terminar por eles os transforma no
 * que são, uma nota de rodapé que responde "e o meu e-mail?".
 *
 * Os dois campos travados vêm com a explicação embaixo, e não apenas apagados: um campo cinza sem
 * motivo parece defeito, e a pergunta que ele levanta — "por que não posso mudar?" — é exatamente a
 * que vira mensagem de suporte.
 *
 * Estado e cidade **abrem uma tela** em vez de aceitarem digitação, e mostram a escolha na mesma
 * forma em que ela foi feita — `São Paulo - SP`. O banco guarda só a sigla; o nome por extenso é
 * remontado na carga, uma vez.
 *
 * **Salvar confirma e fica.** A tela fechava sozinha ao gravar, sem dizer nada: a pessoa tocava em
 * "Salvar", tudo sumia, e não havia como distinguir "salvou" de "voltou sem salvar". Agora aparece
 * a confirmação e a tela permanece — quem veio corrigir um dado precisa **ver** a correção pegar, e
 * a seta de voltar está no topo para quem já terminou.
 *
 * @param actions os eventos da tela, reunidos: com estado e cidade a assinatura passaria de seis
 *   parâmetros, e seis lambdas soltas em sequência é onde duas trocadas de lugar compilam.
 */
@Composable
internal fun AccountScreen(state: AccountUiState, actions: AccountActions, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.student_saved)

    LaunchedEffect(state.saved) {
        if (!state.saved) return@LaunchedEffect

        // Baixa o sinal **antes** de esperar o aviso sumir: sem isso, uma segunda gravação enquanto
        // o primeiro aviso ainda está na tela não dispararia o efeito de novo.
        actions.onSavedShown()
        snackbarHostState.showSnackbar(message = savedMessage)
    }

    AppScreenScaffold(
        title = stringResource(R.string.student_account_title),
        modifier = modifier,
        onBack = actions.onBack,
        backContentDescription = stringResource(R.string.student_action_back),
        snackbarHostState = snackbarHostState,
    ) {
        // Um indicador, e não a tela em branco que havia aqui — branco não é "carregando", é
        // "quebrado". Ele só aparece se a carga demorar; ver `AppLoadingState`.
        if (state.loading) {
            AppLoadingState(contentDescription = stringResource(R.string.student_loading))
            return@AppScreenScaffold
        }

        AccountFields(state = state, actions = actions)

        if (state.failed) {
            AppMessageCard(text = stringResource(R.string.student_save_failed))
        }

        AppButton(
            text = stringResource(R.string.student_action_save),
            onClick = actions.onSubmit,
            loading = state.saving,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Os seis campos, na ordem **editável primeiro, fixo depois**.
 *
 * Os dois travados vêm com a explicação embaixo, e não apenas apagados: um campo cinza sem motivo
 * parece defeito, e a pergunta que ele levanta — "por que não posso mudar?" — é exatamente a que
 * vira mensagem de suporte.
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
            label = stringResource(R.string.student_field_name),
            supportingText = stringResource(R.string.student_field_name_support),
            errorMessage = state.nameError?.message(),
        )

        AppMaskedTextField(
            value = state.phone,
            onValueChange = actions.onPhoneChange,
            label = stringResource(R.string.student_field_phone),
            mask = AccountFormValidation.PHONE_MASK,
            supportingText = stringResource(R.string.student_field_phone_support),
            errorMessage = state.phoneError?.message(),
            imeAction = ImeAction.Done,
        )

        AppSelectField(
            value = state.selectedState?.label.orEmpty(),
            label = stringResource(R.string.student_field_state),
            onClick = actions.onOpenStatePicker,
            supportingText = stringResource(R.string.student_field_state_support),
            errorMessage = state.stateError?.message(),
        )

        AppSelectField(
            value = state.city,
            label = stringResource(R.string.student_field_city),
            onClick = actions.onOpenCityPicker,
            supportingText = stringResource(
                if (state.selectedState == null) {
                    R.string.student_field_city_needs_state
                } else {
                    R.string.student_field_city_support
                },
            ),
            errorMessage = state.cityError?.message(),
            enabled = state.selectedState != null,
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

@Preview(name = "Meus dados · claro", showBackground = true, heightDp = 1180)
@Preview(
    name = "Meus dados · escuro",
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
                name = "Ana Ribeiro",
                phone = "11987654321",
                email = "ana@exemplo.com",
                birthDate = "21/05/1990",
                stateUf = "SP",
                stateName = "São Paulo",
                city = "São José dos Campos",
            ),
            actions = previewAccountActions(),
        )
    }
}

/**
 * A conta que ainda não tem localidade — o caso de toda conta criada antes de o campo existir.
 * É o que se confere: a cidade travada precisa parecer "ainda não", e não "quebrado".
 */
@Preview(name = "Meus dados · sem localidade", showBackground = true, heightDp = 1180)
@Composable
private fun AccountScreenWithoutLocationPreview() {
    RunAndLiftTheme {
        AccountScreen(
            state = AccountUiState(
                loading = false,
                name = "Ana Ribeiro",
                email = "ana@exemplo.com",
                birthDate = "21/05/1990",
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
    onSavedShown = {},
    onBack = {},
)
