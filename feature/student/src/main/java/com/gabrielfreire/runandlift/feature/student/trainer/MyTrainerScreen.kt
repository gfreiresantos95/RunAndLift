package com.gabrielfreire.runandlift.feature.student.trainer

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppNoticeCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O treinador do aluno: o que existe hoje, ou o campo que cria isso.
 *
 * **Os dois assuntos nunca aparecem juntos.** Com um vínculo vigente na tela, o campo de código
 * sumiria de qualquer forma na hora de pedir — e mostrá-lo ali seria convidar alguém a trocar de
 * treinador com um toque, no lugar onde ele veio conferir o que já tem.
 *
 * O histórico fica no fim e é só leitura. Um vínculo encerrado não é uma opção para retomar com um
 * toque: retomar é pedir de novo, com o código, exatamente como da primeira vez.
 */
@Composable
internal fun MyTrainerScreen(state: MyTrainerUiState, actions: MyTrainerActions, modifier: Modifier = Modifier) {
    AppScreenScaffold(
        title = stringResource(R.string.student_trainer_title),
        modifier = modifier,
        onBack = actions.onBack,
        backContentDescription = stringResource(R.string.student_trainer_back),
    ) {
        when {
            state.loading -> AppLoadingState(contentDescription = stringResource(R.string.student_trainer_loading))
            else -> MyTrainerContent(state = state, actions = actions)
        }
    }
}

/**
 * O miolo, emitido **direto** no `ColumnScope` do scaffold.
 *
 * Sem coluna própria: o conteúdo do [AppScreenScaffold] já chega dentro de um `AppScreenColumn`, com
 * a rolagem e o espaçamento aplicados. Abrir outro aqui aninharia duas rolagens verticais, e a de
 * dentro seria medida com altura infinita — que não desalinha nada, derruba a tela.
 */
@Composable
private fun MyTrainerContent(state: MyTrainerUiState, actions: MyTrainerActions) {
    if (state.failed) {
        AppMessageCard(text = stringResource(R.string.student_trainer_failed))
    }

    val current = state.current

    if (current != null) {
        TrainerLinkCard(
            link = current,
            enabled = !state.submitting,
            onStatusChange = { status -> actions.onStatusChange(current, status) },
        )
    } else {
        CodeEntry(state = state, actions = actions)
    }

    PastLinks(state = state)
}

/** O campo de código e o que ele produz: um erro, ou um treinador para confirmar. */
@Composable
private fun CodeEntry(state: MyTrainerUiState, actions: MyTrainerActions) {
    AppNoticeCard(text = stringResource(R.string.student_trainer_explanation))

    AppTextField(
        value = state.code,
        onValueChange = actions.onCodeChange,
        label = stringResource(R.string.student_trainer_code),
        // O erro do código fica no campo, e não num aviso solto: é ali que se corrige.
        errorMessage = state.error?.takeIf { state.invite == null }?.message(),
        supportingText = stringResource(R.string.student_trainer_code_support),
        enabled = !state.checking && !state.submitting,
        imeAction = ImeAction.Done,
        onImeAction = actions.onSubmitCode,
    )

    AppButton(
        text = stringResource(R.string.student_trainer_search),
        onClick = actions.onSubmitCode,
        enabled = state.canSubmitCode,
        loading = state.checking,
    )

    val invite = state.invite

    if (invite != null) {
        InviteConfirmationCard(
            invite = invite,
            submitting = state.submitting,
            onConfirm = actions.onConfirmInvite,
            onDismiss = actions.onDismissInvite,
        )

        // A falha do pedido aparece junto do cartão, e não no campo: o que está errado ali não é
        // o que foi digitado.
        state.error?.let { AppMessageCard(text = it.message()) }
    }
}

@Composable
private fun PastLinks(state: MyTrainerUiState) {
    if (state.past.isEmpty()) return

    Text(
        text = stringResource(R.string.student_trainer_past),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    state.past.forEach { link ->
        Text(
            text = link.trainerName.ifBlank { stringResource(R.string.student_trainer_unnamed) },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(name = "Meu treinador · claro", showBackground = true, heightDp = 720)
@Preview(
    name = "Meu treinador · escuro",
    showBackground = true,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MyTrainerScreenPreview() {
    RunAndLiftTheme {
        // Sem vínculo: é o estado em que a tela tem mais coisa — aviso, campo, botão e histórico.
        MyTrainerScreen(state = previewNoTrainerState(), actions = previewMyTrainerActions())
    }
}
