package com.gabrielfreire.runandlift.feature.trainer.programeditor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppCounterField
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * A prescrição de um exercício: séries, faixa de repetições, carga, descanso e o recado.
 *
 * **Tela e não caixa de diálogo.** São cinco campos, e uma caixa que some ao toque fora dela é o
 * pior lugar possível para um formulário — é a mesma razão pela qual o código de convite tem tela
 * própria.
 *
 * **Os números são contadores, não campos de digitação.** Preenche-se isto cinco vezes por exercício
 * e quarenta vezes por programa; um teclado numérico que sobe e desce a cada campo transforma dois
 * minutos de trabalho em dez. Ver `AppCounterField`. A carga é a exceção — ela é decimal e varia
 * muito, então é campo de texto com teclado numérico.
 */
@Composable
internal fun PrescriptionScreen(
    exercise: PrescribedExercise,
    onChange: (PrescribedExercise) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScreenScaffold(
        title = exercise.exerciseName,
        modifier = modifier,
        onBack = onDone,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        AppCounterField(
            value = exercise.sets,
            onValueChange = { onChange(exercise.copy(sets = it)) },
            label = stringResource(R.string.trainer_prescription_sets),
            decrementLabel = stringResource(R.string.trainer_prescription_decrement),
            incrementLabel = stringResource(R.string.trainer_prescription_increment),
            range = SETS_RANGE,
        )

        RepRangeFields(exercise = exercise, onChange = onChange)

        AppCounterField(
            value = exercise.restSeconds ?: 0,
            onValueChange = { onChange(exercise.copy(restSeconds = it.takeIf { value -> value > 0 })) },
            label = stringResource(R.string.trainer_prescription_rest_label),
            decrementLabel = stringResource(R.string.trainer_prescription_decrement),
            incrementLabel = stringResource(R.string.trainer_prescription_increment),
            range = REST_RANGE,
            step = REST_STEP,
            suffix = stringResource(R.string.trainer_prescription_seconds),
        )

        AppTextField(
            value = exercise.loadKg?.let(::loadToText).orEmpty(),
            onValueChange = { onChange(exercise.copy(loadKg = it.toLoadOrNull())) },
            label = stringResource(R.string.trainer_prescription_load_label),
            supportingText = stringResource(R.string.trainer_prescription_load_support),
            keyboardType = KeyboardType.Decimal,
        )

        AppTextField(
            value = exercise.notes.orEmpty(),
            onValueChange = { onChange(exercise.copy(notes = it.takeIf(String::isNotBlank))) },
            label = stringResource(R.string.trainer_prescription_notes),
            supportingText = stringResource(R.string.trainer_prescription_notes_support),
        )

        AppButton(
            text = stringResource(R.string.trainer_prescription_done),
            onClick = onDone,
            modifier = Modifier.padding(top = Dimens.SpaceSmall),
        )
    }
}

/**
 * A faixa de repetições: dois contadores que se empurram.
 *
 * **O máximo empurra o mínimo, e o mínimo empurra o máximo.** Sem isso, subir o mínimo acima do
 * máximo produz "12 a 8", que é uma faixa que não existe. Empurrar em vez de bloquear é o que
 * permite ir de "8 a 12" para "12 a 15" sem passar por um estado recusado — bloquear obrigaria a
 * mexer no máximo primeiro, que é uma ordem que ninguém adivinha.
 */
@Composable
private fun RepRangeFields(exercise: PrescribedExercise, onChange: (PrescribedExercise) -> Unit) {
    AppCounterField(
        value = exercise.minReps,
        onValueChange = { value ->
            onChange(exercise.copy(minReps = value, maxReps = maxOf(value, exercise.maxReps)))
        },
        label = stringResource(R.string.trainer_prescription_min_reps),
        decrementLabel = stringResource(R.string.trainer_prescription_decrement),
        incrementLabel = stringResource(R.string.trainer_prescription_increment),
        range = REPS_RANGE,
    )

    AppCounterField(
        value = exercise.maxReps,
        onValueChange = { value ->
            onChange(exercise.copy(maxReps = value, minReps = minOf(value, exercise.minReps)))
        },
        label = stringResource(R.string.trainer_prescription_max_reps),
        decrementLabel = stringResource(R.string.trainer_prescription_decrement),
        incrementLabel = stringResource(R.string.trainer_prescription_increment),
        range = REPS_RANGE,
    )

    Text(
        text = stringResource(R.string.trainer_prescription_reps_support),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * O texto do campo virando carga, ou nada.
 *
 * Aceita vírgula **e** ponto: o teclado decimal do Android entrega o separador do idioma do
 * aparelho, e um treinador com o telefone em inglês digitaria ponto num app em português. Recusar
 * um dos dois seria transformar a configuração do celular em erro de digitação.
 *
 * Texto vazio é `null`, e não zero — "sem carga prescrita" e "prescrevi zero quilos" são coisas
 * diferentes, e a segunda não existe.
 */
private fun String.toLoadOrNull(): Double? = replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }

/** Carga sem casa decimal quando ela é inteira, e com vírgula quando não é. */
private fun loadToText(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')

/** Dez séries já é muito; cem é erro de digitação que o contador não deveria permitir alcançar. */
private val SETS_RANGE = 1..20

/** Uma repetição é válido (força máxima); cinquenta cobre resistência e isometria contada. */
private val REPS_RANGE = 1..50

/**
 * Descanso começa em zero, ao contrário dos outros.
 *
 * Zero aqui significa "não prescrevi", e é resposta legítima — em circuito ou aquecimento não há
 * descanso a combinar. Cinco minutos é o teto do que se descansa entre séries de força máxima.
 */
private val REST_RANGE = 0..300

/** De quinze em quinze: ninguém prescreve 47 segundos de descanso. */
private const val REST_STEP = 15

@Preview(name = "Prescrição · claro", showBackground = true, heightDp = 1000)
@Preview(
    name = "Prescrição · escuro",
    showBackground = true,
    heightDp = 1000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PrescriptionScreenPreview() {
    RunAndLiftTheme {
        Column {
            PrescriptionScreen(
                exercise = previewPrescriptions().first(),
                onChange = {},
                onDone = {},
            )
        }
    }
}
