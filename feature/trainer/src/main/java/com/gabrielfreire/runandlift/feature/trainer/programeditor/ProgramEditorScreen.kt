package com.gabrielfreire.runandlift.feature.trainer.programeditor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppListRow
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppNoticeCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppSectionHeader
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O editor do programa: nome, objetivo, observação e a lista de dias.
 *
 * **Os dias são linhas que abrem outra tela**, e não seções expansíveis aqui dentro. Um programa
 * ABC com seis exercícios por dia daria uma rolagem de sessenta linhas nesta tela, e o campo do nome
 * — que é o único obrigatório — ficaria a um palmo do topo de tudo. A tela de dia carrega o dia; esta
 * carrega o programa.
 *
 * **Salvar exige só o nome.** Montar um programa leva dias, e um app que se recusa a guardar
 * trabalho pela metade ensina a pessoa a não confiar nele. O que falta para o programa poder ir para
 * um aluno aparece como aviso, não como bloqueio — e aparece **aqui**, e não só na hora de atribuir,
 * porque descobrir ali é descobrir tarde.
 */
@Composable
internal fun ProgramEditorScreen(
    state: ProgramEditorUiState,
    actions: ProgramEditorActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScreenScaffold(
        title = stringResource(R.string.trainer_program_title),
        modifier = modifier,
        onBack = onBack,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        when {
            state.loading -> AppLoadingState(
                contentDescription = stringResource(R.string.trainer_programs_loading),
            )

            state.notFound -> AppEmptyState(
                title = stringResource(R.string.trainer_program_missing_title),
                description = stringResource(R.string.trainer_program_missing),
            )

            else -> ProgramEditorForm(state = state, actions = actions)
        }
    }
}

@Composable
private fun ColumnScope.ProgramEditorForm(state: ProgramEditorUiState, actions: ProgramEditorActions) {
    if (state.saveFailed) {
        AppMessageCard(text = stringResource(R.string.trainer_program_save_failed))
    }

    AppTextField(
        value = state.program.name,
        onValueChange = actions.onNameChange,
        label = stringResource(R.string.trainer_program_name),
        supportingText = stringResource(R.string.trainer_program_name_support),
    )

    GoalPicker(selected = state.program.goal, onSelect = { actions.onGoalChange(it) })

    AppTextField(
        value = state.program.notes.orEmpty(),
        onValueChange = actions.onNotesChange,
        label = stringResource(R.string.trainer_program_notes),
        supportingText = stringResource(R.string.trainer_program_notes_support),
    )

    AppSectionHeader(
        title = stringResource(R.string.trainer_program_days),
        modifier = Modifier.padding(top = Dimens.SpaceSmall),
    )

    if (state.program.days.isEmpty()) {
        Text(
            text = stringResource(R.string.trainer_program_days_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    state.program.days.forEachIndexed { index, day ->
        DayRow(day = day, onClick = { actions.onOpenDay(index) })
    }

    AppOutlinedButton(
        text = stringResource(R.string.trainer_program_add_day),
        onClick = actions.onAddDay,
    )

    if (state.incomplete) {
        AppNoticeCard(text = stringResource(R.string.trainer_program_incomplete_notice))
    }

    AppButton(
        text = stringResource(R.string.trainer_action_save),
        onClick = actions.onSave,
        enabled = state.canSave,
        loading = state.saving,
        modifier = Modifier.padding(top = Dimens.SpaceSmall),
    )

    // Atribuir só existe para programa já gravado: a cópia que o aluno recebe sai do documento, e
    // não do rascunho — dar a alguém uma edição que ainda não foi salva entregaria um treino que
    // não existe em `programs`. Por isso o botão some no programa novo, em vez de ficar apagado.
    if (state.canAssign) {
        AppOutlinedButton(
            text = stringResource(R.string.trainer_program_assign),
            onClick = actions.onAssign,
        )
    }
}

/**
 * Um dia na lista do programa.
 *
 * O apoio conta exercícios e séries — que é o tamanho do dia — e troca por um aviso quando o dia
 * está vazio, porque um dia sem exercício é o que impede o programa inteiro de ser atribuído.
 */
@Composable
private fun DayRow(day: ProgramDay, onClick: () -> Unit) {
    AppListRow(
        title = day.focus ?: stringResource(R.string.trainer_day_untitled),
        supportingText = if (day.isEmpty) {
            stringResource(R.string.trainer_day_empty)
        } else {
            stringResource(
                R.string.trainer_day_size,
                pluralStringResource(
                    R.plurals.trainer_program_exercises,
                    day.exercises.size,
                    day.exercises.size,
                ),
                pluralStringResource(R.plurals.trainer_day_sets, day.totalSets, day.totalSets),
            )
        },
        leading = day.label,
        onClick = onClick,
    )
}

/**
 * Um programa pela metade — com dia vazio e sem objetivo —, que é o estado em que a tela tem algo a
 * dizer. O programa pronto não mostra o aviso, e é o preview que menos ensina.
 */
@Preview(name = "Editor de programa · claro", showBackground = true, heightDp = 1000)
@Preview(
    name = "Editor de programa · escuro",
    showBackground = true,
    heightDp = 1000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProgramEditorScreenPreview() {
    RunAndLiftTheme {
        Column {
            ProgramEditorScreen(
                state = previewEditorState(),
                actions = previewEditorActions(),
                onBack = {},
            )
        }
    }
}
