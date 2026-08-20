package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * O título de um bloco dentro de uma tela — "Sua semana", "Sua carteira".
 *
 * Nasce com o painel das duas homes, que é a primeira tela do produto com **mais de um assunto na
 * mesma rolagem**. Até aqui toda tela tinha um assunto só e o título da barra superior bastava;
 * empilhar quatro cards sem dizer do que cada grupo trata devolve uma parede de números.
 *
 * **É cabeçalho para o leitor de tela, e não só texto grande.** O `heading` é o que permite navegar
 * de seção em seção no TalkBack em vez de ouvir a tela inteira em ordem — numa tela feita de blocos
 * independentes, essa é a diferença entre percorrer e procurar.
 *
 * @param support a linha de baixo, opcional. Serve ao que precisa de ressalva junto do título — é
 *   por onde o painel diz que os números ainda são exemplo, em vez de deixar quem lê descobrir
 *   sozinho que aquele treino não é dele.
 */
@Composable
fun AppSectionHeader(title: String, modifier: Modifier = Modifier, support: String? = null) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )

        if (support != null) {
            Text(
                text = support,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Com e sem a linha de apoio: o segundo é o caso comum, e o primeiro é onde se confere que a
 * ressalva não fica com o mesmo peso do título.
 */
@LightDarkPreviews
@Composable
private fun AppSectionHeaderPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                AppSectionHeader(
                    title = PreviewSamples.Dashboard.SECTION_TITLE,
                    support = PreviewSamples.Dashboard.SECTION_SUPPORT,
                )
                AppSectionHeader(title = PreviewSamples.Dashboard.ROSTER_TITLE)
            }
        }
    }
}
