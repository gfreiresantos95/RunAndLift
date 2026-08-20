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
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppNoticeCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppSectionHeader
import com.gabrielfreire.runandlift.core.designsystem.extendedColors
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O painel inteiro do treinador, na ordem em que ele se lê.
 *
 * A ordem responde sempre à mesma pergunta — **o que ele faz com isto?**
 *
 * 1. **A carteira**, que é o único bloco com dado real e a resposta a "como está o meu trabalho".
 * 2. **A semana**, que diz se o que foi prescrito virou treino.
 * 3. **Quem precisa de atenção**, que é o único bloco que muda o que ele faz hoje.
 *
 * O bloco de atenção fica por último de propósito, e é o oposto do que a intuição sugere. Ele é uma
 * lista de nomes com altura variável: no topo, empurraria as contagens para fora da primeira tela
 * numa semana ruim, que é exatamente a semana em que se quer ver as duas coisas.
 *
 * **Só a carteira não diz "exemplo".** Ela vem de `links`, e chamar de exemplo o número real de
 * alunos de alguém seria pior do que não mostrá-lo.
 *
 * @param roster as contagens reais, ou `null` quando a leitura falhou. `null` não vira uma fileira
 *   de zeros: "você tem 0 alunos" dito a um treinador com trinta é a pior frase que esta tela pode
 *   produzir, e é a mesma regra que a aba de alunos já segue.
 */
@Composable
internal fun TrainerDashboardSection(
    roster: TrainerRoster?,
    dashboard: TrainerDashboard,
    modifier: Modifier = Modifier,
) {
    val sample = stringResource(R.string.trainer_home_sample)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        AppSectionHeader(title = stringResource(R.string.trainer_home_roster_title))
        RosterBlock(roster = roster)

        AppSectionHeader(title = stringResource(R.string.trainer_home_week_title), support = sample)
        WeekMetrics(dashboard = dashboard)

        AppSectionHeader(
            title = stringResource(R.string.trainer_home_attention_title),
            support = pluralStringResource(
                R.plurals.trainer_home_attention_support,
                dashboard.attentionCount,
                dashboard.attentionCount,
            ),
        )
        AppNoticeCard(text = sample)
        dashboard.attention.forEach { AttentionRow(item = it) }
    }
}

/**
 * A carteira, ou a explicação de por que ela não está aqui.
 *
 * A falha usa `attention` e não `critical`: não conseguir contar os alunos não impede nada do que
 * o treinador faz nesta tela, e vermelho aqui gastaria o vermelho que a próxima tela vai precisar.
 */
@Composable
private fun RosterBlock(roster: TrainerRoster?) {
    if (roster == null) {
        AppMessageCard(
            text = stringResource(R.string.trainer_home_roster_failed),
            role = MaterialTheme.extendedColors.attention,
        )
    } else {
        RosterMetrics(roster = roster)
    }
}

/** O painel com a carteira lida. A falha de leitura está no preview da tela inteira. */
@LightDarkPreviews
@Composable
private fun TrainerDashboardSectionPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                TrainerDashboardSection(
                    roster = previewRoster(),
                    dashboard = TrainerDashboard.SAMPLE,
                )
            }
        }
    }
}
