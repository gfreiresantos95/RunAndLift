package com.gabrielfreire.runandlift.feature.student.home

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
import com.gabrielfreire.runandlift.feature.student.R

/**
 * Os quatro números da semana do aluno, em duas fileiras de dois.
 *
 * A ordem não é decorativa. **Treinos e sequência vêm primeiro** porque medem constância, que é o
 * que decide se alguém ainda está usando o app daqui a um ano; volume e tempo vêm depois porque
 * medem esforço, que varia por mil razões e não se compara entre semanas sem contexto.
 *
 * A sequência é o único número pintado — com `highlight`, o papel de conquista do tema. E vem com o
 * rótulo escrito, como todo uso de cor com significado: quem não distingue a cor lê "semanas
 * seguidas" do mesmo jeito.
 *
 * O apoio de cada peça diz o que o número **significa**, e não o que ele é. "12.480 kg" sozinho não
 * se lê como bom nem ruim; "carga total levantada" ao menos diz do que se trata.
 */
@Composable
internal fun WeekMetrics(dashboard: StudentDashboard, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        AppMetricRow {
            AppMetricTile(
                value = stringResource(
                    R.string.student_home_sessions_value,
                    dashboard.sessionsDone,
                    dashboard.sessionsPlanned,
                ),
                label = stringResource(R.string.student_home_sessions_label),
                support = dashboard.sessionsSupport(),
                modifier = Modifier.weight(weight = 1f),
            )
            AppMetricTile(
                value = dashboard.streakWeeks.toString(),
                label = pluralStringResource(
                    R.plurals.student_home_streak_label,
                    dashboard.streakWeeks,
                ),
                support = stringResource(R.string.student_home_streak_support),
                role = MaterialTheme.extendedColors.highlight,
                modifier = Modifier.weight(weight = 1f),
            )
        }

        AppMetricRow {
            AppMetricTile(
                value = stringResource(
                    R.string.student_home_volume_value,
                    dashboard.volumeKg.asGroupedNumber(),
                ),
                label = stringResource(R.string.student_home_volume_label),
                support = stringResource(R.string.student_home_volume_support),
                modifier = Modifier.weight(weight = 1f),
            )
            AppMetricTile(
                value = dashboard.activeMinutes.asDuration(),
                label = stringResource(R.string.student_home_time_label),
                support = stringResource(R.string.student_home_time_support),
                modifier = Modifier.weight(weight = 1f),
            )
        }
    }
}

/**
 * A linha de apoio dos treinos: quantos faltam, ou que a semana fechou.
 *
 * É a única frase do painel que pede alguma coisa de quem lê, e por isso ela troca em vez de somir:
 * "semana completa" é o recibo de quem cumpriu, e sem ele a peça diria "faltam 0 treinos".
 */
@Composable
private fun StudentDashboard.sessionsSupport(): String = if (weekComplete) {
    stringResource(R.string.student_home_sessions_complete)
} else {
    pluralStringResource(R.plurals.student_home_sessions_remaining, remainingSessions, remainingSessions)
}

@LightDarkPreviews
@Composable
private fun WeekMetricsPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                WeekMetrics(dashboard = StudentDashboard.SAMPLE)
            }
        }
    }
}
