package com.gabrielfreire.runandlift.feature.trainer.programeditor

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.MetricTextStyles
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * Um exercício prescrito, dentro do dia: o nome, os números e o que dá para fazer com ele.
 *
 * **Os números ficam numa linha só e em dígitos tabulares** — "4 × 8-12 · 60 kg · 90 s". É a forma
 * que uma planilha de academia usa, e é a que se lê de relance numa lista de oito; quebrá-los em
 * rótulos ("Séries: 4") dobraria a altura de cada linha para dizer o que a ordem já diz.
 *
 * **Subir e descer são botões, e não arrastar.** A ordem é a de execução e importa — composto antes
 * de isolado —, mas arrastar exige uma lista preguiçosa com estado de reordenação, que é decisão do
 * `:core` e não desta tela. Dois botões resolvem o caso real, que é mover um exercício uma posição.
 */
@Composable
internal fun PrescriptionRow(
    exercise: PrescribedExercise,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    actions: PrescriptionRowActions,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = actions.onEdit)
                .padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                text = exercise.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(text = exercise.summary(), style = MetricTextStyles.small)

            if (!exercise.notes.isNullOrBlank()) {
                Text(text = exercise.notes.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTextButton(
                    text = stringResource(R.string.trainer_day_move_up),
                    onClick = actions.onMoveUp,
                    enabled = canMoveUp,
                )
                AppTextButton(
                    text = stringResource(R.string.trainer_day_move_down),
                    onClick = actions.onMoveDown,
                    enabled = canMoveDown,
                )
                AppTextButton(
                    text = stringResource(R.string.trainer_day_remove_exercise),
                    onClick = actions.onRemove,
                )
            }
        }
    }
}

/**
 * A linha de números.
 *
 * Faixa fechada vira número só — "10" e não "10 a 10" —, porque quem pôs o mesmo valor nos dois
 * campos quis um número fixo, e mostrar a faixa devolveria a ele a própria escolha travestida de
 * intervalo. Carga e descanso somem quando não foram prescritos: "sem carga" ocuparia espaço para
 * dizer que não há o que dizer.
 */
@Composable
private fun PrescribedExercise.summary(): String {
    val reps = if (hasFixedReps) {
        minReps.toString()
    } else {
        stringResource(R.string.trainer_prescription_rep_range, minReps, maxReps)
    }

    return listOfNotNull(
        stringResource(R.string.trainer_prescription_sets_reps, sets, reps),
        loadKg?.let { stringResource(R.string.trainer_prescription_load, formatLoad(it)) },
        restSeconds?.let { stringResource(R.string.trainer_prescription_rest, it) },
    ).joinToString(SEPARATOR)
}

/**
 * Carga sem casa decimal quando ela é inteira.
 *
 * "60 kg" e não "60,0 kg": a segunda forma sugere uma precisão que a anilha da academia não tem, e
 * ocupa espaço numa linha que já é densa. A meia casa sobrevive porque existe de verdade — 62,5 kg é
 * a soma de duas anilhas de 1,25.
 */
private fun formatLoad(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')

private const val SEPARATOR = " · "

@LightDarkPreviews
@Composable
private fun PrescriptionRowPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                // O primeiro da lista: é onde se confere que "subir" fica desabilitado.
                PrescriptionRow(
                    exercise = previewPrescriptions().first(),
                    canMoveUp = false,
                    canMoveDown = true,
                    actions = previewRowActions(),
                )
                // Sem carga, sem descanso e sem observação — o mínimo que uma prescrição pode ser.
                PrescriptionRow(
                    exercise = previewBarePrescription(),
                    canMoveUp = true,
                    canMoveDown = false,
                    actions = previewRowActions(),
                )
            }
        }
    }
}
