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
 * Por que o registro é pedido e o que vai ser feito com ele — o bloco do treinador.
 *
 * É a contraparte de [HealthDataNotice], no mesmo desenho e na mesma vaga: lá o app conta o que
 * **não** pede, aqui conta o que **faz** com o que pediu. Nos dois casos a pergunta é respondida
 * antes de ser feita — e a que o treinador faz é "vocês vão publicar isso?".
 */
@Composable
internal fun CrefNotice(modifier: Modifier = Modifier) {
    AppNoticeCard(text = stringResource(id = R.string.auth_cref_notice), modifier = modifier)
}

@LightDarkPreviews
@Composable
private fun CrefNoticePreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                CrefNotice()
            }
        }
    }
}
