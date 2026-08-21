package com.gabrielfreire.runandlift.feature.trainer.assign

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppNoticeCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * Atribuir um programa: a lista de alunos ativos, e quem já está com ele.
 *
 * **Só entram vínculos ativos.** Não é escolha de tela: a regra do Firestore exige vínculo ativo
 * para criar a atribuição, então listar quem está pausado seria oferecer um botão que o servidor
 * recusa — e a pessoa ficaria procurando o erro no lugar errado.
 *
 * **Quem já tem o programa continua na lista**, marcado, com a opção de encerrar. Sumir viraria
 * dúvida, e reatribuir é operação legítima: é o que atualiza o treino de quem já o tinha depois de
 * o molde mudar, porque a cópia é congelada no momento da atribuição.
 *
 * O aviso do topo diz a consequência **antes** do toque: atribuir substitui o treino anterior
 * daquele aluno. É o tipo de coisa que ninguém deveria descobrir depois.
 */
@Composable
internal fun AssignScreen(
    state: AssignUiState,
    actions: AssignActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScreenScaffold(
        title = stringResource(R.string.trainer_assign_title),
        modifier = modifier,
        onBack = onBack,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        when {
            state.loading -> AppLoadingState(
                contentDescription = stringResource(R.string.trainer_assign_loading),
            )

            state.isEmpty && !state.failed -> AppEmptyState(
                title = stringResource(R.string.trainer_assign_empty_title),
                description = stringResource(R.string.trainer_assign_empty),
                icon = AppIcons.Students,
            )

            else -> AssignContent(state = state, actions = actions)
        }
    }
}

@Composable
private fun ColumnScope.AssignContent(state: AssignUiState, actions: AssignActions) {
    if (state.failed) {
        AppMessageCard(text = stringResource(R.string.trainer_assign_failed))
        AppTextButton(text = stringResource(R.string.trainer_assign_retry), onClick = actions.onRetry)
    }

    if (state.assignFailed) {
        AppMessageCard(text = stringResource(R.string.trainer_assign_write_failed))
    }

    state.program?.let { program ->
        Text(
            text = program.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    AppNoticeCard(text = stringResource(R.string.trainer_assign_replaces))

    state.students.forEach { link ->
        StudentAssignRow(
            link = link,
            assigned = state.isAssigned(link),
            working = state.isAssigning(link),
            onAssign = { actions.onAssign(link) },
            onRemove = { actions.onRemove(link) },
        )
    }
}

/**
 * Um aluno na lista: o nome, se já tem o programa, e o que dá para fazer.
 *
 * O estado é **texto**, e não cor — "Está com este treino" escrito, pela mesma regra que a carteira
 * de alunos segue: uma bolinha colorida exigiria legenda e falharia inteira para quem não distingue
 * as duas cores.
 */
@Composable
private fun StudentAssignRow(
    link: Link,
    assigned: Boolean,
    working: Boolean,
    onAssign: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                // Nome vazio acontece: quem entrou pelo Google pode não ter nome gravado, e a lista
                // não pode virar uma sequência de linhas em branco por causa disso.
                text = link.studentName.ifBlank { stringResource(R.string.trainer_assign_unnamed) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (assigned) {
                Text(
                    text = stringResource(R.string.trainer_assign_current),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTextButton(
                    text = stringResource(
                        if (assigned) R.string.trainer_assign_again else R.string.trainer_assign_action,
                    ),
                    onClick = onAssign,
                    enabled = !working,
                )

                if (assigned) {
                    AppTextButton(
                        text = stringResource(R.string.trainer_assign_end),
                        onClick = onRemove,
                        enabled = !working,
                    )
                }
            }
        }
    }
}

/** Com um aluno já atribuído e outro não — os dois estados que a linha precisa saber desenhar. */
@Preview(name = "Atribuir · claro", showBackground = true, heightDp = 800)
@Preview(
    name = "Atribuir · escuro",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AssignScreenPreview() {
    RunAndLiftTheme {
        Column {
            AssignScreen(state = previewAssignState(), actions = previewAssignActions(), onBack = {})
        }
    }
}

/** Sem aluno ativo nenhum: o treinador tem programa e não tem a quem dar. */
@Preview(name = "Atribuir sem alunos · claro", showBackground = true, heightDp = 600)
@Composable
private fun AssignEmptyPreview() {
    RunAndLiftTheme {
        Column {
            AssignScreen(
                state = previewAssignState().copy(students = emptyList()),
                actions = previewAssignActions(),
                onBack = {},
            )
        }
    }
}
