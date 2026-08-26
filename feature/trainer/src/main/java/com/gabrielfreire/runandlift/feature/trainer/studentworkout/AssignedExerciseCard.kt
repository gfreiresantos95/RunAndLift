package com.gabrielfreire.runandlift.feature.trainer.studentworkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.MetricTextStyles
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.text.summary

/**
 * Um exercício da prescrição de um aluno, do jeito que o treinador confere o que entregou.
 *
 * **Não é clicável.** É o mesmo desenho do `PrescriptionRow` do editor sem a fileira de botões:
 * aqui não há o que mover nem remover, porque o que está na tela é a **cópia congelada** que o aluno
 * recebeu — mexer nela por engano trocaria o treino de alguém que está na academia. Quem edita é o
 * editor de programa, e a mudança chega ao aluno pela reatribuição.
 *
 * O número entra antes do nome pela mesma razão do lado do aluno: numa lista de oito, é por ele que
 * se acha do que se está falando.
 *
 * @param position a posição na ordem de execução, começando em 1.
 */
@Composable
internal fun AssignedExerciseCard(position: Int, exercise: PrescribedExercise, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                text = stringResource(R.string.trainer_student_workout_exercise, position, exercise.exerciseName),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Dígitos tabulares: numa lista de oito, uma coluna que dança a cada valor é o que
            // impede o olho de descer por ela.
            Text(text = exercise.summary(), style = MetricTextStyles.small)

            if (!exercise.notes.isNullOrBlank()) {
                Text(text = exercise.notes.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@LightDarkPreviews
@Composable
private fun AssignedExerciseCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                // Completo: faixa de repetições, carga quebrada e recado do treinador.
                AssignedExerciseCard(position = 1, exercise = previewAssignedExercises().first())
                // O mínimo que uma prescrição pode ser: séries, faixa e nada mais.
                AssignedExerciseCard(position = 2, exercise = previewAssignedExercises().last())
            }
        }
    }
}
