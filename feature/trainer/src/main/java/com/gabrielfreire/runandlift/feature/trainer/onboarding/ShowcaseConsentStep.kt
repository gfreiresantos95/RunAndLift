package com.gabrielfreire.runandlift.feature.trainer.onboarding

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
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * Passo do aceite da vitrine — a porta dos dois últimos.
 *
 * É consentimento **destacado**, e não uma linha somada ao aceite dos termos: publicar nome,
 * cidade, registro e apresentação para quem ainda não é aluno é outra finalidade, com outro
 * público, e finalidade nova pede autorização própria (LGPD art. 8º, §4º). Juntá-lo ao aceite do
 * cadastro transformaria dois consentimentos em um.
 *
 * O aviso vem **antes** da caixa, e nesta ordem de propósito: primeiro o que fica visível, para
 * quem, e como se desliga; só então a pergunta. Caixa antes de explicação é assinatura em folha
 * branca.
 *
 * Recusar não fecha nada além da vitrine: quem não aceita continua treinando os alunos que já tem,
 * e o texto diz isso — é a diferença entre uma escolha e um pedágio.
 */
@Composable
internal fun ShowcaseConsentStep(accepted: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        AppNoticeCard(text = stringResource(R.string.trainer_onboarding_showcase_notice))

        AppCheckboxField(
            checked = accepted,
            onCheckedChange = onChange,
            text = stringResource(R.string.trainer_onboarding_showcase_consent),
        )
    }
}

@LightDarkPreviews
@Composable
private fun ShowcaseConsentStepPreview() {
    RunAndLiftTheme {
        ShowcaseConsentStep(accepted = false, onChange = {})
    }
}
