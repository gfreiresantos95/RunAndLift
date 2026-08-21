package com.gabrielfreire.runandlift.feature.student.workouts

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
import com.gabrielfreire.runandlift.core.designsystem.component.AppListRow
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.feature.student.R

/**
 * Um dia do treino na lista: o rótulo no marcador, o foco na linha, o volume à direita.
 *
 * **O rótulo vira o marcador da esquerda** porque é assim que a planilha de academia se lê: "hoje é
 * o B". Uma letra dentro de um quadrado é exatamente o que `AppListRow.leading` existe para mostrar,
 * e é mais rápido de achar numa lista de seis do que a mesma letra dentro de uma frase.
 *
 * **O dia sem foco escrito não ganha texto de substituição.** "Sem foco definido" acusaria o
 * treinador de ter esquecido algo que é opcional de propósito — quem chama os dias de A, B e C
 * muitas vezes não precisa de mais nada.
 *
 * À direita vai a contagem de exercícios, e não a de séries: é o número que responde "quanto tempo
 * isso vai levar", que é a pergunta de quem está escolhendo entre treinar agora ou depois.
 */
@Composable
internal fun WorkoutDayRow(day: ProgramDay, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppListRow(
        title = stringResource(R.string.student_workout_day_title, day.label),
        modifier = modifier,
        supportingText = day.focus?.takeIf { it.isNotBlank() },
        leading = day.label,
        trailing = pluralStringResource(
            R.plurals.student_workout_day_exercises,
            day.exercises.size,
            day.exercises.size,
        ),
        onClick = onClick,
    )
}

@LightDarkPreviews
@Composable
private fun WorkoutDayRowPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                WorkoutDayRow(day = previewDay(), onClick = {})
                // Sem foco escrito: é onde se confere que a linha não inventa um texto no lugar.
                WorkoutDayRow(day = previewAssignment().days.last(), onClick = {})
            }
        }
    }
}
