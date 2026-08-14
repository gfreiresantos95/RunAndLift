package com.gabrielfreire.runandlift.feature.student.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormActions
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.trainingform.previewTrainingFormActions

/**
 * O passo a passo do onboarding: uma pergunta por tela, com saída em todas.
 *
 * **"Pular" fica visível em todo passo**, e com o mesmo peso visual em todos. Escondê-lo no
 * consentimento — o passo em que ele mais importa — transformaria uma escolha em um obstáculo, que
 * é o oposto do que consentimento significa.
 *
 * Não há seta de voltar. Cada passo é independente do anterior e nada se perde ao seguir; uma seta
 * sugeriria que a resposta anterior precisa ser revista para continuar. O que ficar para trás se
 * corrige depois, na edição de perfil, que é a mesma tela com todos os campos juntos.
 *
 * A barra de progresso é honesta sobre o total mudar: ela cresce quando o consentimento é dado,
 * porque aí passam a existir perguntas que antes não cabia fazer.
 */
@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    form: TrainingFormState,
    actions: TrainingFormActions,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .padding(paddingValues = Dimens.ScreenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
        ) {
            OnboardingHeader(state = state)

            OnboardingStepContent(state = state, form = form, actions = actions)

            Spacer(modifier = Modifier.padding(top = Dimens.SpaceSmall))

            if (state.failed) {
                Text(
                    text = stringResource(R.string.student_onboarding_save_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            AppButton(
                text = if (state.isLast) {
                    stringResource(R.string.student_onboarding_finish)
                } else {
                    stringResource(R.string.student_onboarding_next)
                },
                onClick = onNext,
                loading = state.saving,
            )

            AppTextButton(
                text = stringResource(R.string.student_onboarding_skip),
                onClick = onSkip,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OnboardingHeader(state: OnboardingUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        LinearProgressIndicator(
            progress = { state.position.toFloat() / state.total },
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.student_onboarding_progress, state.position, state.total),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(text = state.step.title(), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = state.step.subtitle(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Onboarding · nível, claro", showBackground = true, heightDp = 720)
@Preview(
    name = "Onboarding · nível, escuro",
    showBackground = true,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OnboardingScreenPreview() {
    RunAndLiftTheme {
        OnboardingScreen(
            state = OnboardingUiState(),
            form = TrainingFormState(),
            actions = previewTrainingFormActions(),
            onNext = {},
            onSkip = {},
        )
    }
}

/** O último passo, com o consentimento já dado — é onde o rótulo do botão vira "Concluir". */
@Preview(name = "Onboarding · último passo", showBackground = true, heightDp = 720)
@Composable
private fun OnboardingLastStepPreview() {
    RunAndLiftTheme {
        OnboardingScreen(
            state = OnboardingUiState(step = OnboardingStep.RESTRICTIONS, position = 6, total = 6),
            form = TrainingFormState(healthConsent = true),
            actions = previewTrainingFormActions(),
            onNext = {},
            onSkip = {},
        )
    }
}
