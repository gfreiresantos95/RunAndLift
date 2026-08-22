package com.gabrielfreire.runandlift.feature.student.workouts

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
import com.gabrielfreire.runandlift.feature.student.R

/**
 * Um exercício prescrito, do jeito que o aluno lê na academia.
 *
 * **Não é clicável, e é de propósito.** Ele é o mesmo desenho do `PrescriptionRow` do treinador sem
 * a fileira de botões: aqui não há o que editar, mover ou remover, e uma linha que responde ao toque
 * prometeria uma tela de detalhe que não existe. Quando o registro de série chegar (E6-02), é este
 * card que ganha o toque — não antes.
 *
 * **A ordem na tela é a ordem de execução**, e é a decisão do treinador. A tela guarda a decisão
 * dele em vez de reordenar por músculo ou por carga.
 *
 * O número entra antes do nome porque na academia se procura "o terceiro" e não "a rosca direta": a
 * pessoa já sabe o que vai fazer, ela está achando onde parou.
 *
 * @param position a posição na ordem de execução, começando em 1.
 */
@Composable
internal fun PrescriptionCard(position: Int, exercise: PrescribedExercise, modifier: Modifier = Modifier) {
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
                text = stringResource(R.string.student_workout_exercise_name, position, exercise.exerciseName),
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

/**
 * A linha de números — "4 × 8-12 · 60 kg · 90 s".
 *
 * Uma linha e não rótulos empilhados: "Séries: 4" dobraria a altura de cada card para dizer o que a
 * ordem já diz, e é a forma que a planilha de academia usa há décadas. A regra que decide o que
 * entra mora em [PrescriptionFormat], fora do composable, porque é ela que tem teste.
 */
@Composable
private fun PrescribedExercise.summary(): String {
    val reps = if (hasFixedReps) {
        minReps.toString()
    } else {
        stringResource(R.string.student_workout_rep_range, minReps, maxReps)
    }

    return PrescriptionFormat.summary(
        listOf(
            stringResource(R.string.student_workout_sets_reps, sets, reps),
            loadKg?.let { stringResource(R.string.student_workout_load, PrescriptionFormat.load(it)) },
            restSeconds?.let { stringResource(R.string.student_workout_rest, it) },
        ),
    )
}

@LightDarkPreviews
@Composable
private fun PrescriptionCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                // Completo: faixa de repetições, carga quebrada e recado do treinador.
                PrescriptionCard(position = 1, exercise = previewFullPrescription())
                // O mínimo que uma prescrição pode ser: séries, repetição fixa e nada mais.
                PrescriptionCard(position = 2, exercise = previewBarePrescription())
            }
        }
    }
}
