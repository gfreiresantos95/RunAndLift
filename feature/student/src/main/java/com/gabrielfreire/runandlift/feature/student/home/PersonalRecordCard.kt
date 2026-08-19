package com.gabrielfreire.runandlift.feature.student.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.MetricTextStyles
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.extendedColors
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O último recorde pessoal, no pé do painel.
 *
 * Fica por último de propósito. Recorde é a coisa mais agradável de olhar e a menos acionável de
 * todas: não diz o que fazer hoje nem como está a semana. No topo, ele empurraria para baixo a
 * única peça que responde "o que eu treino agora".
 *
 * É a peça que usa `highlight`, o papel de conquista do tema — e a única do painel com esse peso,
 * porque um destaque que aparece três vezes na mesma tela deixa de destacar.
 */
@Composable
internal fun PersonalRecordCard(dashboard: StudentDashboard, modifier: Modifier = Modifier) {
    val highlight = MaterialTheme.extendedColors.highlight

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = highlight.container,
        contentColor = highlight.onContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
            ) {
                Text(
                    text = stringResource(R.string.student_home_record_title),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(text = dashboard.recordExercise, style = MaterialTheme.typography.titleMedium)
            }

            Text(text = dashboard.recordLoad, style = MetricTextStyles.medium)
        }
    }
}

@LightDarkPreviews
@Composable
private fun PersonalRecordCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                PersonalRecordCard(dashboard = StudentDashboard.SAMPLE)
            }
        }
    }
}
