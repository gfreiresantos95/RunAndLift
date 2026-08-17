package com.gabrielfreire.runandlift.feature.trainer.invite

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.MetricTextStyles
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppNoticeCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenColumn
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O código de convite, para ler em voz alta ou mandar numa conversa.
 *
 * O código ocupa o centro da tela em corpo grande e com dígitos tabulares ([MetricTextStyles]): ele
 * é ditado por telefone e digitado por outra pessoa, e é a única informação da tela que precisa ser
 * lida sem erro.
 *
 * **O aviso de que o código não é senha vem junto, e não como letra miúda.** Quem entende que ainda
 * vai confirmar cada pedido manda o código sem medo; quem não entende, ou não manda, ou acha que
 * mandou a chave da própria carteira.
 */
@Composable
internal fun InviteScreen(state: InviteUiState, actions: InviteActions, modifier: Modifier = Modifier) {
    AppScreenScaffold(
        title = stringResource(R.string.trainer_invite_title),
        modifier = modifier,
        onBack = actions.onBack,
        backContentDescription = stringResource(R.string.trainer_invite_back),
    ) {
        when {
            state.loading -> AppLoadingState(contentDescription = stringResource(R.string.trainer_invite_loading))
            else -> InviteContent(state = state, actions = actions)
        }
    }
}

@Composable
private fun InviteContent(state: InviteUiState, actions: InviteActions) {
    AppScreenColumn(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge)) {
        if (state.failed) {
            AppMessageCard(text = stringResource(R.string.trainer_invite_failed))
            AppTextButton(text = stringResource(R.string.trainer_invite_retry), onClick = actions.onRetry)
        }

        val code = state.code

        if (code == null) {
            AppNoticeCard(text = stringResource(R.string.trainer_invite_none))
        } else {
            CodeCard(code = code)
            AppNoticeCard(text = stringResource(R.string.trainer_invite_explanation))
            AppButton(
                text = stringResource(R.string.trainer_invite_share),
                onClick = { actions.onShare(code) },
                enabled = !state.working,
            )
        }

        AppTextButton(
            text = stringResource(
                if (code == null) R.string.trainer_invite_generate else R.string.trainer_invite_regenerate,
            ),
            onClick = actions.onGenerate,
            enabled = !state.working,
        )

        // O aviso da troca só aparece quando há o que perder: dito antes do primeiro código, seria
        // uma advertência sobre uma consequência que ainda não existe.
        if (code != null) {
            Text(
                text = stringResource(R.string.trainer_invite_regenerate_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CodeCard(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = code,
            style = MetricTextStyles.large,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = Dimens.SpaceXLarge),
        )
    }
}

@Preview(name = "Convite · claro", showBackground = true, heightDp = 720)
@Preview(name = "Convite · escuro", showBackground = true, heightDp = 720, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InviteScreenPreview() {
    RunAndLiftTheme {
        InviteScreen(
            state = InviteUiState(loading = false, code = "ABC234"),
            actions = previewInviteActions(),
        )
    }
}
