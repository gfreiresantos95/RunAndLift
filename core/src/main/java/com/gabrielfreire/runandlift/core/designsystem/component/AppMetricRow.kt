package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Uma fileira de [AppMetricTile] com a mesma altura e o mesmo respiro entre elas.
 *
 * **A altura igual é a razão de este componente existir.** Peças em `Row` crescem cada uma até o
 * seu conteúdo, e basta um rótulo que quebra em duas linhas para a fileira ficar com um degrau no
 * meio. `IntrinsicSize.Min` mede a mais alta antes de desenhar e entrega essa altura às outras — o
 * que nenhuma tela vai lembrar de fazer sozinha, e nenhuma deveria precisar.
 *
 * **Duas por fileira, e não uma grade que se vira.** Num telefone, três números lado a lado deixam
 * cada um com pouco mais de cem `dp`: o rótulo quebra em três linhas e o número perde o destaque
 * que é a única razão de ele estar num painel. Quem tem quatro números escreve duas fileiras — o
 * componente não impede três peças, mas o padrão é duas.
 *
 * Quem chama distribui a largura com `Modifier.weight(1f)` em cada peça. Fica com quem chama
 * porque é ele quem sabe se as duas valem o mesmo espaço — e às vezes não valem.
 */
@Composable
fun AppMetricRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        content = content,
    )
}

/**
 * Duas peças de alturas diferentes: é exatamente o caso que o componente existe para consertar, e
 * o preview não serve para nada se mostrar duas peças gêmeas.
 */
@LightDarkPreviews
@Composable
private fun AppMetricRowPreview() {
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
                        value = PreviewSamples.Dashboard.STREAK_VALUE,
                        label = PreviewSamples.Dashboard.STREAK_LABEL,
                        modifier = Modifier.weight(weight = 1f),
                    )
                }
            }
        }
    }
}
