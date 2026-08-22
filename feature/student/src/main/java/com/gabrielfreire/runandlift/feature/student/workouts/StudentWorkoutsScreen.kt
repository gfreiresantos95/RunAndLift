package com.gabrielfreire.runandlift.feature.student.workouts

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppListRow
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenColumn
import com.gabrielfreire.runandlift.core.designsystem.component.AppSectionHeader
import com.gabrielfreire.runandlift.core.designsystem.component.AppTabScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.previewTabs
import com.gabrielfreire.runandlift.feature.student.text.title

/**
 * A aba de treinos do aluno: o treino que o treinador prescreveu para ele.
 *
 * As quatro obrigações de tela, e a que mais importa aqui é a última: **falha nunca é desenhada como
 * vazio**. "Seu treinador ainda não montou seu treino" dito a quem tem treino e está sem sinal é
 * pior do que a frase equivalente nas outras telas, porque ela é convincente — manda a pessoa cobrar
 * alguém por algo que já foi feito, em vez de mandar tentar de novo.
 *
 * **O vazio não tem botão**, ao contrário do vazio do treinador, e a assimetria é a dos papéis: quem
 * monta o treino é outra pessoa. Uma ação aqui prometeria um atalho que não existe. O que o texto
 * faz é dizer se a espera é pelo treinador ou se falta se vincular a um.
 *
 * **Os dias são linhas, e não um acordeão.** Um dia com oito exercícios é uma tela cheia; expandi-lo
 * dentro da lista empurraria os outros para fora do alcance e faria a rolagem perder o lugar a cada
 * toque. Um dia de cada vez também é como se lê na academia.
 */
@Composable
internal fun StudentWorkoutsScreen(
    state: StudentWorkoutsUiState,
    tabs: List<AppBottomBarItem>,
    actions: StudentWorkoutsActions,
    modifier: Modifier = Modifier,
) {
    AppTabScaffold(
        title = stringResource(R.string.student_workouts_title),
        tabs = tabs,
        modifier = modifier,
    ) { innerPadding ->
        val content = Modifier.padding(paddingValues = innerPadding)

        when {
            state.loading -> AppLoadingState(
                contentDescription = stringResource(R.string.student_workouts_loading),
                modifier = content,
            )

            state.isEmpty -> AppEmptyState(
                title = stringResource(R.string.student_workouts_empty_title),
                description = stringResource(R.string.student_workouts_empty),
                modifier = content,
            )

            else -> WorkoutContent(state = state, actions = actions, modifier = content)
        }
    }
}

/**
 * O treino aberto: o cabeçalho do programa e os dias.
 *
 * O aviso de falha fica **acima** do treino e não no lugar dele: quem já leu o treino uma vez
 * continua com ele na tela, e a mensagem só acrescenta que a releitura não foi.
 */
@Composable
private fun WorkoutContent(
    state: StudentWorkoutsUiState,
    actions: StudentWorkoutsActions,
    modifier: Modifier = Modifier,
) {
    AppScreenColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)) {
        if (state.failed) {
            AppMessageCard(text = stringResource(R.string.student_workouts_failed))
            AppTextButton(text = stringResource(R.string.student_workouts_retry), onClick = actions.onRetry)
        }

        state.assignment?.let { assignment ->
            WorkoutHeader(assignment = assignment)

            AppSectionHeader(
                title = stringResource(R.string.student_workout_days),
                modifier = Modifier.padding(top = Dimens.SpaceSmall),
            )

            state.days.forEachIndexed { index, day ->
                WorkoutDayRow(day = day, onClick = { actions.onOpenDay(index) })
            }
        }
    }
}

/**
 * O que o treino é: o nome do programa, o objetivo e o recado do treinador.
 *
 * A linha do programa é **de leitura, sem toque e sem seta** — é o que `onClick` nulo faz em
 * `AppListRow`. Ela não leva a lugar nenhum de propósito: o molde vive em `programs`, que o aluno
 * não tem permissão de ler, e uma seta ali prometeria uma tela que a regra do Firestore recusa.
 *
 * **O nome do treinador não aparece**, e a ausência é uma escolha de custo: `Assignment` copia o
 * nome do aluno para dentro, mas não o do treinador, então mostrá-lo custaria uma leitura de
 * `links` a cada abertura da aba. Quem quer saber tem "Meu treinador" no menu, e quem está de pé na
 * academia quer o dia A, não o crachá.
 *
 * A observação fica **antes** dos dias, e não depois: "aquecer 10 minutos" dito no fim da tela chega
 * tarde para quem já desceu até o dia A.
 */
@Composable
private fun WorkoutHeader(assignment: Assignment, modifier: Modifier = Modifier) {
    AppListRow(
        title = assignment.programName,
        modifier = modifier,
        supportingText = assignment.goal?.title(),
    )

    if (!assignment.notes.isNullOrBlank()) {
        Text(
            text = assignment.notes.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Dimens.SpaceSmall, vertical = Dimens.SpaceXSmall),
        )
    }
}

@Preview(name = "Treinos do aluno · claro", showBackground = true, heightDp = 800)
@Preview(
    name = "Treinos do aluno · escuro",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun StudentWorkoutsScreenPreview() {
    RunAndLiftTheme {
        StudentWorkoutsScreen(
            state = StudentWorkoutsUiState(loading = false, assignment = previewAssignment()),
            tabs = previewTabs(StudentTab.WORKOUTS),
            actions = previewWorkoutsActions(),
        )
    }
}

/** O vazio, que é o que o aluno sem treinador vê — e o estado mais comum antes do primeiro vínculo. */
@Preview(name = "Treinos do aluno · vazio", showBackground = true, heightDp = 640)
@Composable
private fun StudentWorkoutsEmptyPreview() {
    RunAndLiftTheme {
        StudentWorkoutsScreen(
            state = StudentWorkoutsUiState(loading = false),
            tabs = previewTabs(StudentTab.WORKOUTS),
            actions = previewWorkoutsActions(),
        )
    }
}

/** Releitura que falhou com o treino já na tela: o aviso entra e o treino fica. */
@Preview(name = "Treinos do aluno · falha", showBackground = true, heightDp = 800)
@Composable
private fun StudentWorkoutsFailedPreview() {
    RunAndLiftTheme {
        StudentWorkoutsScreen(
            state = StudentWorkoutsUiState(loading = false, failed = true, assignment = previewAssignment()),
            tabs = previewTabs(StudentTab.WORKOUTS),
            actions = previewWorkoutsActions(),
        )
    }
}
