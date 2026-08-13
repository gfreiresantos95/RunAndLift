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
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppCheckboxField
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.component.LegalLinks

/**
 * Aceite dos termos e opt-in de comunicação — **duas caixas, as duas desmarcadas**.
 *
 * Separadas porque as finalidades são diferentes e a LGPD exige destaque para cada uma (art. 8º,
 * §4º): condicionar a conta ao aceite dos termos é legítimo; condicioná-la a receber e-mail de
 * marketing não é. Desmarcadas porque caixa pré-marcada não é escolha — é a inércia respondendo
 * pela pessoa.
 *
 * Os documentos ficam acessíveis **antes** do aceite, e não numa tela de configurações depois:
 * concordar com o que não dá para ler é assinar em branco.
 */
@Composable
internal fun SignUpConsentFields(
    form: SignUpFormState,
    formActions: SignUpFormActions,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AppCheckboxField(
            checked = form.acceptedTerms,
            onCheckedChange = formActions.onTermsChange,
            text = stringResource(id = R.string.auth_terms_accept),
            enabled = enabled,
            errorMessage = stringResource(id = R.string.auth_terms_required).takeIf { form.termsMissing },
        )

        LegalLinks(onOpen = formActions.onOpenLegalDocument, enabled = enabled)

        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))

        // A ressalva entra como texto de apoio do próprio campo, e não como um `Text` solto abaixo
        // dele: solto, ela recomeçava alinhada com a margem da tela, e um recuo diferente do
        // rótulo faz a frase parecer de outro item em vez de continuação deste.
        AppCheckboxField(
            checked = form.marketingOptIn,
            onCheckedChange = formActions.onMarketingChange,
            text = stringResource(id = R.string.auth_marketing_opt_in),
            enabled = enabled,
            supportingText = stringResource(id = R.string.auth_marketing_support),
        )
    }
}

/**
 * O aceite em falta, que é o estado que importa: a mensagem de obrigatoriedade precisa caber sem
 * empurrar a caixa de marketing para fora do bloco.
 */
@LightDarkPreviews
@Composable
private fun SignUpConsentFieldsPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                SignUpConsentFields(
                    form = SignUpFormState(termsMissing = true),
                    formActions = previewSignUpFormActions(),
                    enabled = true,
                )
            }
        }
    }
}
