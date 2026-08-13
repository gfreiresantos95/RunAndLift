package com.gabrielfreire.runandlift.feature.auth.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * A rede de segurança do papel: aparece só quando a conta chegou até aqui sem nenhum.
 *
 * Ao contrário das boas-vindas, esta tela **tem estado**: a escolha é confirmada num segundo passo,
 * porque aqui o toque grava em `users/{uid}` em vez de apenas navegar. Um toque acidental que
 * escreve é diferente de um toque acidental de onde se volta.
 */
@Composable
internal fun RoleSelectionScreen(
    state: RoleSelectionUiState,
    onSelect: (ActiveRole) -> Unit,
    onConfirm: () -> Unit,
    onConfirmed: (ActiveRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.confirmedRole) {
        state.confirmedRole?.let(onConfirmed)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = stringResource(R.string.onboarding_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
        ) {
            RoleOptionCard(
                title = stringResource(R.string.onboarding_student),
                description = stringResource(R.string.onboarding_student_description),
                selected = state.selected == ActiveRole.STUDENT,
                onClick = { onSelect(ActiveRole.STUDENT) },
                enabled = !state.submitting,
            )
            RoleOptionCard(
                title = stringResource(R.string.onboarding_trainer),
                description = stringResource(R.string.onboarding_trainer_description),
                selected = state.selected == ActiveRole.TRAINER,
                onClick = { onSelect(ActiveRole.TRAINER) },
                enabled = !state.submitting,
            )
        }

        if (state.failed) {
            Text(
                text = stringResource(R.string.auth_error_unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Start,
            )
        }

        AppButton(
            text = stringResource(R.string.onboarding_confirm),
            onClick = onConfirm,
            enabled = state.selected != null,
            loading = state.submitting,
        )
    }
}

/**
 * O estado em que a tela abre — nada escolhido, botão desabilitado. É o que se confere aqui:
 * "Continuar" só liga depois de existir uma escolha, porque confirmar o vazio não é uma ação.
 */
@Preview(name = "Escolha de papel · claro", showBackground = true, heightDp = 600)
@Preview(
    name = "Escolha de papel · escuro",
    showBackground = true,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun RoleSelectionPreview() {
    RunAndLiftTheme {
        RoleSelectionScreen(
            state = RoleSelectionUiState(),
            onSelect = {},
            onConfirm = {},
            onConfirmed = {},
        )
    }
}

/** Papel escolhido e gravação que falhou: o erro fica em texto, acima do botão, e não some. */
@Preview(name = "Escolha de papel · falha", showBackground = true, heightDp = 600)
@Composable
private fun RoleSelectionFailurePreview() {
    RunAndLiftTheme {
        RoleSelectionScreen(
            state = RoleSelectionUiState(selected = ActiveRole.TRAINER, failed = true),
            onSelect = {},
            onConfirm = {},
            onConfirmed = {},
        )
    }
}
