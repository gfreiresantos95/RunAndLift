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
 * **Só a semana tem cabeçalho.** O primeiro bloco carrega o próprio título dentro do card, e o
 * recorde já diz o que é na primeira linha dele — um cabeçalho para cada peça transformaria a
 * rolagem numa alternância de título e conteúdo em que nada se destaca de nada.
 */
@Composable
internal fun StudentDashboardSection(dashboard: StudentDashboard, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        NextWorkoutCard(dashboard = dashboard)

        AppSectionHeader(title = stringResource(R.string.student_home_week_title))
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
