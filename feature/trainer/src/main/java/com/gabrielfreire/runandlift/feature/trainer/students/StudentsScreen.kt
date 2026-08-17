package com.gabrielfreire.runandlift.feature.trainer.students

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.previewTabs

/**
 * A carteira de alunos: quem pediu, quem treina e quem saiu.
 *
 * **Quem espera resposta vem primeiro**, e é a única razão de a tela ter blocos: um pedido novo
 * perdido no meio de trinta nomes conhecidos é um pedido que fica dias sem resposta. Os encerrados
 * ficam no fim, e ficam — aluno que some de lista sem explicação vira dúvida.
 *
 * Coluna rolável e não lista preguiçosa: o teto de cem vínculos por leitura torna a diferença
 * invisível hoje, e uma lista preguiçosa é decisão do `:core`, não desta tela. O gatilho para
 * revisar é a carteira passar a paginar.
 *
 * As quatro obrigações de tela estão aqui e são quatro coisas distintas: carregando, vazia,
 * conteúdo e **falha** — esta última nunca desenhada como vazia, porque "você ainda não tem alunos"
 * é a pior frase possível para um treinador com trinta alunos e sem sinal.
 */
@Composable
internal fun StudentsScreen(
    state: StudentsUiState,
    tabs: List<AppBottomBarItem>,
    actions: StudentsActions,
    modifier: Modifier = Modifier,
) {
    AppTabScaffold(
        title = stringResource(R.string.trainer_students_title),
        tabs = tabs,
        modifier = modifier,
    ) { innerPadding ->
        val content = Modifier.padding(paddingValues = innerPadding)

        when {
            state.loading -> AppLoadingState(
                contentDescription = stringResource(R.string.trainer_students_loading),
                modifier = content,
            )

            state.isEmpty && !state.failed -> AppEmptyState(
                title = stringResource(R.string.trainer_students_empty_title),
                description = stringResource(R.string.trainer_students_empty),
                modifier = content,
                icon = AppIcons.Students,
                action = stringResource(R.string.trainer_students_invite),
                onAction = actions.onOpenInvite,
            )

            else -> StudentsContent(state = state, actions = actions, modifier = content)
        }
    }
}

@Composable
private fun StudentsContent(state: StudentsUiState, actions: StudentsActions, modifier: Modifier = Modifier) {
    AppScreenColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)) {
        if (state.failed) {
            AppMessageCard(text = stringResource(R.string.trainer_students_failed))
            AppTextButton(text = stringResource(R.string.trainer_students_retry), onClick = actions.onRetry)
        }

        AppOutlinedButton(text = stringResource(R.string.trainer_students_invite), onClick = actions.onOpenInvite)

        StudentsSection(R.string.trainer_students_pending, state.pending, state, actions)
        StudentsSection(R.string.trainer_students_current, state.current, state, actions)
        StudentsSection(R.string.trainer_students_past, state.past, state, actions)
    }
}

/**
 * Um bloco da carteira, ou nada.
 *
 * Bloco vazio não desenha nem o título: uma seção "Aguardando resposta" permanentemente vazia
 * ensina a ignorá-la, e no dia em que tiver alguém ela não chama atenção nenhuma.
 */
@Composable
private fun StudentsSection(title: Int, links: List<Link>, state: StudentsUiState, actions: StudentsActions) {
    if (links.isEmpty()) return

    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Dimens.SpaceMedium, bottom = Dimens.SpaceXSmall),
    )

    links.forEach { link ->
        StudentRow(
            link = link,
            updating = state.isUpdating(link),
            onStatusChange = { status -> actions.onStatusChange(link, status) },
        )
    }
}

@Preview(name = "Alunos · claro", showBackground = true, heightDp = 800)
@Preview(name = "Alunos · escuro", showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StudentsScreenPreview() {
    RunAndLiftTheme {
        StudentsScreen(
            state = previewStudentsState(),
            tabs = previewTabs(TrainerTab.STUDENTS),
            actions = previewStudentsActions(),
        )
    }
}
