package com.gabrielfreire.runandlift.feature.trainer.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppStatusLabel

/**
 * Um aluno que precisa de atenção: o nome, o motivo e onde ele está no semáforo.
 *
 * **O motivo fica na linha de baixo, e não escondido atrás de um toque.** É a informação que decide
 * o que o treinador faz — mandar mensagem, ligar, ou deixar quieto — e um painel que obriga a abrir
 * três telas para descobrir três motivos não economizou tempo de ninguém.
 *
 * O selo do semáforo fica à direita, alinhado ao nome, porque é o que se percorre com o olho ao
 * descer a lista: os motivos têm comprimentos diferentes e não formam coluna.
 *
 * A linha ainda não é clicável — a tela de detalhe do aluno não existe. Quando existir, o alvo é a
 * linha inteira, como na carteira.
 */
@Composable
internal fun AttentionRow(item: AttentionItem, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.ListItemHeight),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(text = item.reason, style = MaterialTheme.typography.bodySmall)
            }

            AppStatusLabel(
                text = item.level.label(),
                role = item.level.role(),
                icon = item.level.icon(),
            )
        }
    }
}

/**
 * Os três níveis empilhados, que é como a lista aparece de verdade — e onde se confere que o selo
 * não empurra o nome para uma segunda linha em tela estreita.
 */
@LightDarkPreviews
@Composable
private fun AttentionRowPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                TrainerDashboard.SAMPLE.attention.forEach { AttentionRow(item = it) }
                AttentionRow(
                    item = AttentionItem(
                        name = "Bruno Lima",
                        reason = "4 de 4 treinos na semana",
                        level = AttentionLevel.OK,
                    ),
                )
            }
        }
    }
}
