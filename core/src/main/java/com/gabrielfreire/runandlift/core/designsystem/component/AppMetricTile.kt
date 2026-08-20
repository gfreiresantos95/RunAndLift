package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.gabrielfreire.runandlift.core.designsystem.ColorRole
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.MetricTextStyles
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.extendedColors

/**
 * Um número medido com o seu rótulo: o tijolo de que os painéis das duas homes são feitos.
 *
 * O número usa [MetricTextStyles], que tem dígitos tabulares — sem eles, uma carteira que passa de
 * 9 para 10 alunos empurra o rótulo para o lado, e uma tela cujos números dançam a cada leitura se
 * lê como instável mesmo quando está certa.
 *
 * **O rótulo fica embaixo, e não em cima.** O olho procura o número primeiro num painel; o rótulo é
 * o que confirma o que ele significa, e confirmação vem depois.
 *
 * **É um nó só para o leitor de tela.** Sem isso o TalkBack dita "12", pausa, "alunos ativos",
 * pausa, "3 entraram este mês" como três coisas soltas, e num painel de quatro peças isso vira
 * doze anúncios sem costura. O `clearAndSetSemantics` junta os três numa frase.
 *
 * @param role pinta o número — e **só** o número. Serve ao painel em que um valor tem estado
 *   (aderência em queda, aluno parado); `null` deixa o número na cor do texto, que é o caso comum.
 *   Como sempre, a cor não carrega significado sozinha: quem usa [role] põe [support] ou um
 *   [AppStatusLabel] ao lado dizendo a mesma coisa por escrito.
 * @param support a terceira linha, menor, para a comparação que dá sentido ao número — "3 a mais
 *   que na semana passada". Um número sem referência não se lê como bom nem ruim.
 */
@Composable
fun AppMetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    support: String? = null,
    role: ColorRole? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = listOfNotNull(value, label, support).joinToString(", ") },
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                text = value,
                style = MetricTextStyles.medium,
                color = role?.color ?: MaterialTheme.colorScheme.onSurface,
            )
            Text(text = label, style = MaterialTheme.typography.bodyMedium)

            if (support != null) {
                Text(text = support, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * Os três formatos lado a lado, dentro de um [AppMetricRow] — que é como eles aparecem de verdade.
 * É aqui que se confere que a peça com três linhas não deixa a vizinha de duas linhas mais baixa.
 */
@LightDarkPreviews
@Composable
private fun AppMetricTilePreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                AppMetricRow {
                    AppMetricTile(
                        value = PreviewSamples.Dashboard.METRIC_VALUE,
                        label = PreviewSamples.Dashboard.METRIC_LABEL,
                        support = PreviewSamples.Dashboard.METRIC_SUPPORT,
                        modifier = Modifier.weight(weight = 1f),
                    )
                    AppMetricTile(
                        value = PreviewSamples.Dashboard.ALERT_VALUE,
                        label = PreviewSamples.Dashboard.ALERT_LABEL,
                        role = MaterialTheme.extendedColors.attention,
                        modifier = Modifier.weight(weight = 1f),
                    )
                }
            }
        }
    }
}
