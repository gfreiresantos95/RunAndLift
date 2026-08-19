package com.gabrielfreire.runandlift.feature.student.home

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
import com.gabrielfreire.runandlift.core.designsystem.component.AppSectionHeader
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O painel inteiro do aluno, na ordem em que ele se lê.
 *
 * Existe para a home não virar um arquivo com quarenta linhas de layout: a tela diz *que* há um
 * painel e onde ele entra, e este arquivo diz do que ele é feito.
 *
 * A ordem é uma decisão, e é sempre a mesma pergunta — **o que a pessoa faz com isto?**
 *
 * 1. **O próximo treino**, que é a razão de alguém abrir o app em pé na academia.
 * 2. **A semana**, que é onde a constância se enxerga e onde mora a única frase que cobra algo.
 * 3. **O recorde**, que é bom de ver e não pede nada.
 *
 * **Cada bloco diz que é exemplo, e diz na linha do título.** Enfiar a ressalva num rodapé cinza no
 * fim da tela seria a mesma coisa que escondê-la: quem lê "12.480 kg" e acredita já acreditou antes
 * de chegar ao rodapé.
 */
@Composable
internal fun StudentDashboardSection(dashboard: StudentDashboard, modifier: Modifier = Modifier) {
    val sample = stringResource(R.string.student_home_sample)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        AppSectionHeader(title = stringResource(R.string.student_home_next_workout_title), support = sample)
        NextWorkoutCard(dashboard = dashboard)

        AppSectionHeader(title = stringResource(R.string.student_home_week_title), support = sample)
        WeekStrip(dashboard = dashboard)
        WeekMetrics(dashboard = dashboard)

        PersonalRecordCard(dashboard = dashboard)
    }
}

@LightDarkPreviews
@Composable
private fun StudentDashboardSectionPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                StudentDashboardSection(dashboard = StudentDashboard.SAMPLE)
            }
        }
    }
}
