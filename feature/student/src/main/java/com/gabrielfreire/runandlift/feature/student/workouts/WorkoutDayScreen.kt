package com.gabrielfreire.runandlift.feature.student.workouts

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.feature.student.R

/**
 * Um dia do treino: os exercícios na ordem de execução.
 *
 * **Nenhuma leitura acontece aqui.** O dia vem do estado da aba, que continua viva na pilha, porque
 * a prescrição inteira chega dentro de um documento só — abrir seis dias custa o mesmo que abrir
 * nenhum. É a regra 2 do orçamento de leitura (§2.4) rendendo o que prometia.
 *
 * O foco vai **abaixo do título**, e não dentro dele: "Treino A · Peito e tríceps" numa barra de
 * aplicativo vira reticências no primeiro aparelho estreito, e o pedaço cortado é justamente o que
 * diz o que se treina.
 *
 * @param day o dia, ou `null` se a posição não existir mais — o treinador reatribuiu um programa
 *   mais curto enquanto esta tela estava aberta, ou o processo foi recriado com a rota antiga. A
 *   tela diz isso em vez de estourar ou de abrir vazia.
 */
@Composable
internal fun WorkoutDayScreen(day: ProgramDay?, onBack: () -> Unit, modifier: Modifier = Modifier) {
    AppScreenScaffold(
        title = day?.let { stringResource(R.string.student_workout_day_title, it.label) }
            ?: stringResource(R.string.student_workout_day_fallback_title),
        modifier = modifier,
        onBack = onBack,
        backContentDescription = stringResource(R.string.student_action_back),
    ) {
        if (day == null) {
            AppMessageCard(text = stringResource(R.string.student_workout_day_gone))
            return@AppScreenScaffold
        }

        day.focus?.takeIf { it.isNotBlank() }?.let { focus ->
            Text(
                text = focus,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = pluralStringResource(R.plurals.student_workout_day_sets, day.totalSets, day.totalSets),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Um dia sem exercício não deveria ter sido atribuído — `Program.isAssignable` barra isso do
        // outro lado —, mas a prescrição é uma cópia congelada e pode ter vindo de antes da regra.
        if (day.isEmpty) {
            AppMessageCard(text = stringResource(R.string.student_workout_day_empty))
        }

        day.exercises.forEachIndexed { index, exercise ->
            PrescriptionCard(position = index + 1, exercise = exercise)
        }
    }
}

@Preview(name = "Dia de treino · claro", showBackground = true, heightDp = 900)
@Preview(
    name = "Dia de treino · escuro",
    showBackground = true,
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun WorkoutDayScreenPreview() {
    RunAndLiftTheme {
        Column { WorkoutDayScreen(day = previewDay(), onBack = {}) }
    }
}

/** O dia que sumiu entre a lista e o toque: é o estado que só se vê quando o treinador reatribui. */
@Preview(name = "Dia de treino · ausente", showBackground = true, heightDp = 400)
@Composable
private fun MissingWorkoutDayPreview() {
    RunAndLiftTheme {
        Column { WorkoutDayScreen(day = null, onBack = {}) }
    }
}
