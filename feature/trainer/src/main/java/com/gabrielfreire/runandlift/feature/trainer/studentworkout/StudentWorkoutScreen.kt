package com.gabrielfreire.runandlift.feature.trainer.studentworkout

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
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppNoticeCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppSectionHeader
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O treino que um aluno recebeu, visto pelo treinador.
 *
 * **É a tela que faltava depois de atribuir.** Antes, prescrever era uma escrita sem volta visível:
 * o treinador escolhia o aluno na tela de atribuição, via a linha dizer "está com este treino", e
 * não tinha por onde reler o que aquela pessoa ficou tendo — nem o nome do programa, nem os dias,
 * nem os exercícios dentro deles.
 *
 * **Os dias são seções abertas, e não linhas que abrem outra tela.** É o oposto do editor, e por um
 * motivo: aqui não se edita nada, e a pergunta é de conferência — "o que eu dei para esta pessoa?".
 * Fechar os dias obrigaria a abrir três telas para responder uma pergunta que a rolagem responde. O
 * documento inteiro já está em memória, então nenhuma dessas seções custa leitura.
 *
 * As quatro obrigações de tela, e a quarta é a que importa aqui: falha de leitura **nunca** é
 * desenhada como "este aluno não tem treino". A frase errada manda o treinador prescrever de novo o
 * que ele já prescreveu — e reatribuir substitui a cópia que o aluno está usando na academia.
 */
@Composable
internal fun StudentWorkoutScreen(
    state: StudentWorkoutUiState,
    actions: StudentWorkoutActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScreenScaffold(
        // Título fixo, e o nome da pessoa no conteúdo. O nome só existe depois da leitura — ele vem
        // de dentro da atribuição —, e uma barra superior que troca de texto no meio do carregamento
        // pisca justamente no elemento que deveria ser o ponto fixo da tela.
        title = stringResource(R.string.trainer_student_workout_title),
        modifier = modifier,
        onBack = onBack,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        when {
            state.loading -> AppLoadingState(
                contentDescription = stringResource(R.string.trainer_student_workout_loading),
            )

            state.failed -> WorkoutFailure(actions = actions)

            state.isEmpty -> AppEmptyState(
                title = stringResource(R.string.trainer_student_workout_empty_title),
                description = stringResource(R.string.trainer_student_workout_empty),
                icon = AppIcons.Workouts,
            )

            else -> WorkoutContent(state = state, actions = actions)
        }
    }
}

/**
 * A leitura que não respondeu, com a saída.
 *
 * Aviso e não vazio, e com "tentar de novo" logo abaixo: um aviso que só informa deixa a pessoa
 * fechando e reabrindo o app para conseguir o mesmo efeito.
 */
@Composable
private fun ColumnScope.WorkoutFailure(actions: StudentWorkoutActions) {
    AppMessageCard(text = stringResource(R.string.trainer_student_workout_failed))
    AppTextButton(text = stringResource(R.string.trainer_student_workout_retry), onClick = actions.onRetry)
}

/** O treino: cabeçalho do programa e um bloco por dia. */
@Composable
private fun ColumnScope.WorkoutContent(state: StudentWorkoutUiState, actions: StudentWorkoutActions) {
    val assignment = state.assignment ?: return

    // Encerrado continua na tela, dito com todas as letras. O documento que fica é justamente o que
    // permite dizer "este treino acabou" em vez de a tela esvaziar de um dia para o outro.
    if (state.isEnded) {
        AppNoticeCard(text = stringResource(R.string.trainer_student_workout_ended))
    }

    Text(
        // Nome vazio acontece: quem entrou pelo Google pode não ter um gravado.
        text = state.studentName.ifBlank { stringResource(R.string.trainer_students_unnamed) },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Text(
        text = assignment.programName.ifBlank { stringResource(R.string.trainer_program_unnamed) },
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Text(
        text = stringResource(
            R.string.trainer_program_size,
            pluralStringResource(R.plurals.trainer_program_days, state.days.size, state.days.size),
            pluralStringResource(
                R.plurals.trainer_program_exercises,
                state.totalExercises,
                state.totalExercises,
            ),
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    if (!assignment.notes.isNullOrBlank()) {
        Text(text = assignment.notes.orEmpty(), style = MaterialTheme.typography.bodyMedium)
    }

    state.days.forEach { day -> DaySection(day = day) }

    // Trocar o treino é ato do editor de programa, e o caminho para lá começa na aba de treinos.
    // O botão fica no fim, depois de a pessoa ter visto o que já existe — antes dela, seria um
    // convite a substituir sem conferir.
    AppTextButton(
        text = stringResource(R.string.trainer_student_workout_change),
        onClick = actions.onOpenPrograms,
        modifier = Modifier.padding(top = Dimens.SpaceLarge),
    )
}

/**
 * Um dia: o cabeçalho com a letra e o foco, e os exercícios na ordem de execução.
 *
 * O dia vazio se anuncia em vez de aparecer como um cabeçalho seguido de nada — ele não deveria
 * existir numa atribuição (a tela de atribuir o recusa), e vê-lo aqui é informação.
 */
@Composable
private fun ColumnScope.DaySection(day: ProgramDay) {
    AppSectionHeader(
        title = stringResource(R.string.trainer_day_title, day.label),
        modifier = Modifier.padding(top = Dimens.SpaceMedium),
        support = day.focus?.takeIf { it.isNotBlank() },
    )

    if (day.isEmpty) {
        Text(
            text = stringResource(R.string.trainer_day_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    day.exercises.forEachIndexed { index, exercise ->
        AssignedExerciseCard(position = index + 1, exercise = exercise)
    }
}

@Preview(name = "Treino do aluno · claro", showBackground = true, heightDp = 1200)
@Preview(
    name = "Treino do aluno · escuro",
    showBackground = true,
    heightDp = 1200,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun StudentWorkoutScreenPreview() {
    RunAndLiftTheme {
        Column {
            StudentWorkoutScreen(
                state = previewStudentWorkoutState(),
                actions = previewStudentWorkoutActions(),
                onBack = {},
            )
        }
    }
}

/** O aluno aceito ontem, para quem ainda não se prescreveu nada — que é o estado mais comum. */
@Preview(name = "Aluno sem treino · claro", showBackground = true, heightDp = 600)
@Composable
private fun StudentWorkoutEmptyPreview() {
    RunAndLiftTheme {
        Column {
            StudentWorkoutScreen(
                state = previewStudentWorkoutState().copy(assignment = null),
                actions = previewStudentWorkoutActions(),
                onBack = {},
            )
        }
    }
}
