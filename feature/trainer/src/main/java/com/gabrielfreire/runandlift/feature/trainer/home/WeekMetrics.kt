package com.gabrielfreire.runandlift.feature.trainer.home

import androidx.compose.foundation.layout.Arrangement
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
import com.gabrielfreire.runandlift.core.designsystem.component.AppMetricRow
import com.gabrielfreire.runandlift.core.designsystem.component.AppMetricTile
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * A semana de trabalho do treinador em quatro números.
 *
 * A aderência média é o único com cor, e a cor vem do semáforo — que é o conceito do produto, e não
 * uma escala inventada nesta tela. Ela carrega o rótulo escrito junto, como sempre; e o `[level]`
 * que a escolhe mora no dado, e não aqui, para que um teste alcance o corte entre "em dia" e
 * "escorregando" sem abrir uma tela.
 *
 * **Treino entregue e sessão registrada aparecem separados de propósito.** São os dois lados do
 * mesmo trabalho: nove treinos prescritos que ninguém executou e nove treinos com trinta e quatro
 * sessões registradas contam histórias opostas, e um número só as somaria numa média que esconde as
 * duas.
 */
@Composable
internal fun WeekMetrics(dashboard: TrainerDashboard, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        AppMetricRow {
            AppMetricTile(
                value = stringResource(R.string.trainer_home_adherence_value, dashboard.adherence),
                label = stringResource(R.string.trainer_home_adherence_label),
                support = dashboard.level.label(),
                role = dashboard.level.role(),
                modifier = Modifier.weight(weight = 1f),
            )
            AppMetricTile(
                value = dashboard.workoutsDelivered.toString(),
                label = stringResource(R.string.trainer_home_workouts_label),
                support = stringResource(R.string.trainer_home_workouts_support),
                modifier = Modifier.weight(weight = 1f),
            )
        }

        AppMetricRow {
            AppMetricTile(
                value = dashboard.sessionsLogged.toString(),
                label = stringResource(R.string.trainer_home_sessions_label),
                support = stringResource(R.string.trainer_home_sessions_support),
                modifier = Modifier.weight(weight = 1f),
            )
            AppMetricTile(
                value = stringResource(
                    R.string.trainer_home_checkins_value,
                    dashboard.checkInsAnswered,
                    dashboard.checkInsSent,
                ),
                label = stringResource(R.string.trainer_home_checkins_label),
                support = stringResource(R.string.trainer_home_checkins_support),
                modifier = Modifier.weight(weight = 1f),
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun WeekMetricsPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                WeekMetrics(dashboard = TrainerDashboard.SAMPLE)
            }
        }
    }
}
