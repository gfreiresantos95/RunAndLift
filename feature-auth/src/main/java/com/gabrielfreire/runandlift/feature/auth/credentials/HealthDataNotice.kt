package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppNoticeCard
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * O que o cadastro **não** pede — o bloco do aluno.
 *
 * Fica na tela, e não só na política de privacidade, porque a preocupação com dado de saúde aparece
 * exatamente aqui: no formulário de um app de treino, na hora de entregar dados. Responder antes
 * de a pergunta ser feita é mais barato do que responder depois, no suporte.
 *
 * Peso, medidas e restrições são dado pessoal sensível (LGPD art. 5º, II) e pertencem à anamnese,
 * com base legal e consentimento próprios — o que este aviso diz em voz alta.
 *
 * O desenho vem de `AppNoticeCard`: ele e o aviso do treinador eram o mesmo `Surface` escrito duas
 * vezes, e duas cópias divergem no dia em que uma delas ganha um recuo diferente.
 */
@Composable
internal fun HealthDataNotice(modifier: Modifier = Modifier) {
    AppNoticeCard(text = stringResource(id = R.string.auth_health_notice), modifier = modifier)
}

@LightDarkPreviews
@Composable
private fun HealthDataNoticePreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                HealthDataNotice()
            }
        }
    }
}
