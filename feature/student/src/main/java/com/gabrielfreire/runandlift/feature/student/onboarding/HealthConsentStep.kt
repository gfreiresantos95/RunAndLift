package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppCheckboxField
import com.gabrielfreire.runandlift.core.designsystem.component.AppNoticeCard
import com.gabrielfreire.runandlift.feature.student.R

/**
 * Passo do consentimento de dado de saúde — a porta dos dois últimos.
 *
 * É consentimento **destacado**, e não uma linha somada ao aceite dos termos: peso, altura e
 * histórico de lesão são dado pessoal sensível, cujo tratamento exige finalidade específica e
 * autorização própria (LGPD art. 11, I; art. 8º, §4º). Juntá-lo ao aceite do cadastro transformaria
 * dois consentimentos em um.
 *
 * O aviso vem **antes** da caixa, e nesta ordem de propósito: primeiro o que será guardado, para
 * quê e quem vê; só então a pergunta. Caixa antes de explicação é assinatura em folha branca.
 *
 * Marcar acrescenta os dois passos seguintes; não marcar encerra o fluxo aqui, sem cobrança.
 */
@Composable
internal fun HealthConsentStep(accepted: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        AppNoticeCard(text = stringResource(R.string.student_onboarding_health_notice))

        AppCheckboxField(
            checked = accepted,
            onCheckedChange = onChange,
            text = stringResource(R.string.student_onboarding_health_consent),
        )
    }
}

@LightDarkPreviews
@Composable
private fun HealthConsentStepPreview() {
    RunAndLiftTheme {
        HealthConsentStep(accepted = false, onChange = {})
    }
}
