package com.gabrielfreire.runandlift.feature.trainer.programeditor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppSectionHeader
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O editor de um dia: o rótulo, o foco e os exercícios na ordem de execução.
 *
 * **Não tem botão de salvar**, e é de propósito: o dia é parte do programa, e o programa é salvo na
 * tela de trás. Um "salvar" aqui daria a entender que existe gravação parcial — e faria a pessoa
 * perguntar o que acontece se ela salvar o dia e não salvar o programa.
 *
 * **Voltar preserva tudo**, porque o rascunho vive no ViewModel da tela de programa, que continua na
 * pilha. É o que permite abrir o catálogo, escolher, voltar e continuar sem nada se perder.
 *
 * A ordem dos exercícios é a de execução, e é editável: composto antes de isolado é a conta que o
 * treinador faz ao montar, e o app guarda a decisão dele em vez de refazê-la.
 */
@Composable
internal fun DayEditorScreen(
    day: ProgramDay,
    actions: DayEditorActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScreenScaffold(
        title = stringResource(R.string.trainer_day_title, day.label),
        modifier = modifier,
        onBack = onBack,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        AppTextField(
            value = day.label,
            onValueChange = { actions.onInfoChange(it, day.focus.orEmpty()) },
            label = stringResource(R.string.trainer_day_label),
            supportingText = stringResource(R.string.trainer_day_label_support),
        )

        AppTextField(
            value = day.focus.orEmpty(),
            onValueChange = { actions.onInfoChange(day.label, it) },
            label = stringResource(R.string.trainer_day_focus),
            supportingText = stringResource(R.string.trainer_day_focus_support),
        )

        AppSectionHeader(
            title = stringResource(R.string.trainer_day_exercises),
            modifier = Modifier.padding(top = Dimens.SpaceSmall),
        )

        if (day.isEmpty) {
            Text(
                text = stringResource(R.string.trainer_day_exercises_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        day.exercises.forEachIndexed { index, exercise ->
            PrescriptionRow(
                exercise = exercise,
                canMoveUp = index > 0,
                canMoveDown = index < day.exercises.lastIndex,
                actions = PrescriptionRowActions(
                    onEdit = { actions.onOpenExercise(index) },
                    onMoveUp = { actions.onMoveUp(index) },
                    onMoveDown = { actions.onMoveDown(index) },
                    onRemove = { actions.onRemoveExercise(index) },
                ),
            )
        }

        AppOutlinedButton(
            text = stringResource(R.string.trainer_day_add_exercise),
            onClick = actions.onAddExercise,
        )

        // Remover o dia inteiro é ação destrutiva e fica por último, longe do resto e em botão de
        // texto: é o que impede o dedo de encontrá-la enquanto procura "adicionar exercício".
        AppTextButton(
            text = stringResource(R.string.trainer_day_remove),
            onClick = actions.onRemoveDay,
            modifier = Modifier.padding(top = Dimens.SpaceLarge),
        )
    }
}

/** O dia vazio, aberto para nada. É o estado logo depois de "adicionar dia" e o que mais aparece. */
@Composable
private fun EmptyDayPreviewContent() {
    DayEditorScreen(day = ProgramDay(label = "B"), actions = previewDayActions(), onBack = {})
}

@Preview(name = "Editor de dia · claro", showBackground = true, heightDp = 1000)
@Preview(
    name = "Editor de dia · escuro",
    showBackground = true,
    heightDp = 1000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DayEditorScreenPreview() {
    RunAndLiftTheme {
        Column {
            DayEditorScreen(day = previewDay(), actions = previewDayActions(), onBack = {})
        }
    }
}

@Preview(name = "Dia vazio · claro", showBackground = true, heightDp = 700)
@Composable
private fun EmptyDayEditorPreview() {
    RunAndLiftTheme {
        Column { EmptyDayPreviewContent() }
    }
}
