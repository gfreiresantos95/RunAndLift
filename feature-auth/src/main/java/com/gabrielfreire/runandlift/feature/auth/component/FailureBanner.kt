package com.gabrielfreire.runandlift.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.feature.auth.message

/**
 * Falha do servidor, acima do botão e não em snackbar: mensagem que some sozinha é mensagem que o
 * usuário menos digital não chega a ler (D11).
 *
 * O fundo de erro é reforço, não o recado — o texto sozinho já diz tudo, o que mantém a regra de
 * cor nunca ser o único canal (E0-09).
 */
@Composable
internal fun FailureBanner(failure: AuthFailure, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = failure.message(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
        )
    }
}

/**
 * A falha curta e a mais longa do conjunto, uma sobre a outra: a de conta Google ausente ocupa três
 * linhas e é ela que revela se o recuo do banner aguenta texto de verdade.
 */
@LightDarkPreviews
@Composable
private fun FailureBannerPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
            ) {
                FailureBanner(failure = AuthFailure.EMAIL_ALREADY_IN_USE)
                FailureBanner(failure = AuthFailure.NO_GOOGLE_ACCOUNT)
            }
        }
    }
}
