package com.gabrielfreire.runandlift.feature.trainer.catalog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.MetricTextStyles
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppSectionHeader
import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.text.label

/**
 * A ficha de um exercício: para que serve, com o quê, e como se executa.
 *
 * **Sem mídia**, e isso é decisão desta fase: a base importada não traz imagem utilizável, e um
 * espaço reservado para uma foto que nunca chega é pior do que não haver espaço nenhum. Os campos
 * `mediaUrl` e `thumbUrl` continuam no modelo, esperando a biblioteca de vídeo.
 *
 * **Os passos são numerados**, e é por isso que `instructions` é uma lista e não texto corrido: um
 * parágrafo único com quatro frases é o formato em que ninguém acha onde parou ao olhar do banco
 * para o celular.
 *
 * As características ficam numa grade de rótulo e valor, e não em chips: chip é para escolher, e
 * aqui não há nada a escolher. Usar o mesmo desenho das duas coisas ensinaria que chip às vezes é
 * clicável e às vezes não.
 */
@Composable
internal fun ExerciseDetailScreen(
    exercise: Exercise?,
    loading: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScreenScaffold(
        title = exercise?.name ?: stringResource(R.string.trainer_exercise_title),
        modifier = modifier,
        onBack = onBack,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        when {
            loading -> AppLoadingState(contentDescription = stringResource(R.string.trainer_catalog_loading))

            exercise == null -> AppEmptyState(
                title = stringResource(R.string.trainer_exercise_missing_title),
                description = stringResource(R.string.trainer_exercise_missing),
            )

            else -> ExerciseDetailContent(exercise = exercise)
        }
    }
}

/**
 * O miolo, emitido **direto** no `ColumnScope` do scaffold.
 *
 * Extensão de `ColumnScope` e não uma `Column` própria: o conteúdo do `AppScreenScaffold` já chega
 * dentro de um `AppScreenColumn`, com rolagem e espaçamento aplicados, e abrir outra coluna aqui
 * perderia esse espaçamento. O escopo no receptor é também o que satisfaz o `compose-lints` —
 * emitir várias coisas no topo é permitido justamente para quem declara em que layout está.
 */
@Composable
private fun ColumnScope.ExerciseDetailContent(exercise: Exercise) {
    Attribute(
        label = stringResource(R.string.trainer_exercise_primary_muscles),
        value = exercise.muscleGroups.joinToString(", "),
    )

    if (exercise.secondaryMuscleGroups.isNotEmpty()) {
        Attribute(
            label = stringResource(R.string.trainer_exercise_secondary_muscles),
            value = exercise.secondaryMuscleGroups.joinToString(", "),
        )
    }

    exercise.equipment?.let {
        Attribute(label = stringResource(R.string.trainer_exercise_equipment), value = it)
    }
    exercise.level?.let {
        Attribute(label = stringResource(R.string.trainer_exercise_level), value = it.label())
    }
    exercise.mechanic?.let {
        Attribute(label = stringResource(R.string.trainer_exercise_mechanic), value = it.label())
    }
    exercise.force?.let {
        Attribute(label = stringResource(R.string.trainer_exercise_force), value = it.label())
    }

    AppSectionHeader(
        title = stringResource(R.string.trainer_exercise_instructions),
        modifier = Modifier.padding(top = Dimens.SpaceMedium),
    )

    exercise.instructions.forEachIndexed { index, step ->
        Step(number = index + 1, text = step)
    }
}

/** Uma característica: rótulo à esquerda, valor à direita. */
@Composable
private fun Attribute(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(width = LABEL_WIDTH),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Um passo da execução, com o número em dígito tabular para a coluna de texto não dançar. */
@Composable
private fun Step(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
    ) {
        Text(text = "$number.", style = MetricTextStyles.small)
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Largura fixa do rótulo, para os valores alinharem numa coluna só. */
private val LABEL_WIDTH = 140.dp

@Preview(name = "Exercício · claro", showBackground = true, heightDp = 900)
@Preview(
    name = "Exercício · escuro",
    showBackground = true,
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ExerciseDetailScreenPreview() {
    RunAndLiftTheme {
        Column {
            ExerciseDetailScreen(exercise = previewExercises().first(), loading = false, onBack = {})
        }
    }
}

/** O exercício que não existe mais no catálogo — acontece depois de uma republicação. */
@Preview(name = "Exercício ausente · claro", showBackground = true, heightDp = 500)
@Composable
private fun ExerciseDetailMissingPreview() {
    RunAndLiftTheme {
        Column { ExerciseDetailScreen(exercise = null, loading = false, onBack = {}) }
    }
}
