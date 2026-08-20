package com.gabrielfreire.runandlift.feature.trainer.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppMetricRow
import com.gabrielfreire.runandlift.core.designsystem.component.AppMetricTile
import com.gabrielfreire.runandlift.core.designsystem.extendedColors
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * As quatro contagens da carteira — **os únicos números do painel que vêm do banco**.
 *
 * Alunos ativos vem primeiro porque é a resposta à pergunta que um treinador faz sobre si mesmo. Os
 * pedidos vêm em segundo por outro motivo: é o único número que representa alguém esperando por
 * ele, e esperar tem prazo.
 *
 * **O pedido pendente é pintado, e só quando existe.** Zero pedidos em âmbar é um alarme sobre
 * nada, que é como se aprende a ignorar alarme; com pedido de verdade, a cor vem acompanhada do
 * rótulo escrito, como todo uso de cor com significado (E0-09).
 *
 * Pausados e encerrados dividem a segunda fileira porque descrevem quem não está treinando agora —
 * um por escolha reversível, outro por decisão tomada.
 */
@Composable
internal fun RosterMetrics(roster: TrainerRoster, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        AppMetricRow {
            AppMetricTile(
                value = roster.active.toString(),
                label = pluralStringResource(R.plurals.trainer_home_active_label, roster.active),
                support = stringResource(R.string.trainer_home_active_support, roster.size),
                modifier = Modifier.weight(weight = 1f),
            )
            AppMetricTile(
                value = roster.pending.toString(),
                label = pluralStringResource(R.plurals.trainer_home_pending_label, roster.pending),
                support = stringResource(R.string.trainer_home_pending_support),
                role = MaterialTheme.extendedColors.attention.takeIf { roster.pending > 0 },
                modifier = Modifier.weight(weight = 1f),
            )
        }

        AppMetricRow {
            AppMetricTile(
                value = roster.paused.toString(),
                label = stringResource(R.string.trainer_home_paused_label),
                support = stringResource(R.string.trainer_home_paused_support),
                modifier = Modifier.weight(weight = 1f),
            )
            AppMetricTile(
                value = roster.ended.toString(),
                label = stringResource(R.string.trainer_home_ended_label),
                support = stringResource(R.string.trainer_home_ended_support),
                modifier = Modifier.weight(weight = 1f),
            )
        }
    }
}

/**
 * Uma carteira com pedido em aberto, que é onde a cor entra — e a carteira vazia, que é como a home
 * abre para quem acabou de criar a conta e o estado em que uma fileira de zeros precisa não
 * parecer defeito.
 */
@LightDarkPreviews
@Composable
private fun RosterMetricsPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                RosterMetrics(roster = previewRoster())
                RosterMetrics(roster = TrainerRoster(links = emptyList()))
            }
        }
    }
}
