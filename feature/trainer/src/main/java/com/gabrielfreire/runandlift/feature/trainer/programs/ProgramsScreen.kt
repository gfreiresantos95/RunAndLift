package com.gabrielfreire.runandlift.feature.trainer.programs

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenColumn
import com.gabrielfreire.runandlift.core.designsystem.component.AppTabScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.previewTabs

/**
 * A aba de treinos do treinador: os moldes que ele montou.
 *
 * **O vazio tem ação, ao contrário do vazio do aluno**, e a diferença é o papel: para o treinador,
 * treino é coisa que ele cria; para o aluno, coisa que ele recebe. O texto que já estava aqui
 * prometia exatamente isso ("os programas que você montar ficam nesta aba") e agora existe o botão
 * que cumpre a promessa.
 *
 * As quatro obrigações de tela: carregando, vazia, conteúdo e falha — e **falha nunca é desenhada
 * como vazio**. "Você ainda não montou nenhum programa" dito a quem tem doze e está sem sinal é o
 * pior que a tela pode dizer.
 *
 * O botão de criar fica **no topo do conteúdo**, e não flutuando: um botão flutuante cobre a última
 * linha da lista, que é justamente onde fica o programa mais antigo — o mais provável de se querer
 * abrir para copiar.
 */
@Composable
internal fun ProgramsScreen(
    state: ProgramsUiState,
    tabs: List<AppBottomBarItem>,
    actions: ProgramsActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
) {
    AppTabScaffold(
        title = stringResource(R.string.trainer_workouts_title),
        tabs = tabs,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        val content = Modifier.padding(paddingValues = innerPadding)

        when {
            state.loading -> AppLoadingState(
                contentDescription = stringResource(R.string.trainer_programs_loading),
                modifier = content,
            )

            state.isEmpty && !state.failed -> AppEmptyState(
                title = stringResource(R.string.trainer_workouts_empty_title),
                description = stringResource(R.string.trainer_workouts_empty),
                modifier = content,
                icon = AppIcons.Workouts,
                action = stringResource(R.string.trainer_program_create),
                onAction = actions.onCreate,
            )

            else -> ProgramsContent(state = state, actions = actions, modifier = content)
        }
    }
}

@Composable
private fun ProgramsContent(state: ProgramsUiState, actions: ProgramsActions, modifier: Modifier = Modifier) {
    AppScreenColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)) {
        if (state.failed) {
            AppMessageCard(text = stringResource(R.string.trainer_programs_failed))
            AppTextButton(text = stringResource(R.string.trainer_programs_retry), onClick = actions.onRetry)
        }

        AppOutlinedButton(
            text = stringResource(R.string.trainer_program_create),
            onClick = actions.onCreate,
        )

        state.programs.forEach { program ->
            ProgramRow(program = program, onClick = { actions.onOpen(program) })
        }
    }
}

@Preview(name = "Treinos do treinador · claro", showBackground = true, heightDp = 800)
@Preview(
    name = "Treinos do treinador · escuro",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProgramsScreenPreview() {
    RunAndLiftTheme {
        ProgramsScreen(
            state = previewProgramsState(),
            tabs = previewTabs(TrainerTab.WORKOUTS),
            actions = previewProgramsActions(),
        )
    }
}
