package com.gabrielfreire.runandlift.feature.auth.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * Título e frase de apoio de uma tela do fluxo de entrada.
 *
 * Alinhado à esquerda mesmo estando dentro de uma coluna centralizada: título centralizado com
 * duas linhas de subtítulo abaixo cria um bloco com duas margens diferentes, e o olho perde onde a
 * leitura recomeça. Começando os dois na mesma margem, o bloco lê como um parágrafo só.
 */
@Composable
internal fun AuthHeadline(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Start,
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(top = Dimens.SpaceXSmall),
        )
    }
}

/**
 * O subtítulo do cadastro de aluno, que é o mais longo do fluxo: se o alinhamento do bloco aguenta
 * duas linhas, aguenta os outros.
 */
@LightDarkPreviews
@Composable
private fun AuthHeadlinePreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                AuthHeadline(
                    title = stringResource(id = R.string.auth_sign_up_title),
                    subtitle = stringResource(id = R.string.auth_sign_up_subtitle_student),
                )
            }
        }
    }
}
