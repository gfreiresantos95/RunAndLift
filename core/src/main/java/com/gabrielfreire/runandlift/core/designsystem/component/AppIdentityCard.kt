package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Card que apresenta quem está usando o app: uma saudação, uma linha de contexto e um monograma.
 *
 * É a primeira coisa da home dos dois papéis, e o que muda entre eles é só o texto — daí morar
 * aqui e não em um dos dois módulos de feature, que não se enxergam.
 *
 * **O monograma chega pronto.** Extrair a inicial de um nome parece trivial e não é: nome composto,
 * nome com preposição e nome vazio têm respostas diferentes, e essa é uma regra de produto. O design
 * system desenha o círculo; quem sabe o que escrever nele é a feature.
 *
 * @param greeting a saudação com o nome, já montada — "Olá, Ana".
 * @param subtitle a linha de baixo, em tom secundário. Tipicamente o papel de quem está logado.
 * @param monogram uma ou duas letras para o círculo, ou `null` quando ainda não há nome. Sem ele o
 *   círculo não é desenhado: um círculo vazio ao lado de "Olá," anuncia um carregamento que não
 *   está acontecendo.
 */
@Composable
fun AppIdentityCard(greeting: String, subtitle: String, modifier: Modifier = Modifier, monogram: String? = null) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (monogram != null) {
                Monogram(text = monogram)
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun Monogram(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(Dimens.AvatarSmall),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = CircleShape,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Com e sem monograma: o segundo é o estado real de quem entrou pelo Google e ainda não tem nome
 * gravado, e é nele que se confere se o card não fica torto sem o círculo.
 */
@LightDarkPreviews
@Composable
private fun AppIdentityCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                AppIdentityCard(
                    greeting = PreviewSamples.Identity.GREETING,
                    subtitle = PreviewSamples.Identity.ROLE_STUDENT,
                    monogram = "A",
                )
                AppIdentityCard(
                    greeting = PreviewSamples.Identity.GREETING,
                    subtitle = PreviewSamples.Identity.ROLE_TRAINER,
                )
            }
        }
    }
}
